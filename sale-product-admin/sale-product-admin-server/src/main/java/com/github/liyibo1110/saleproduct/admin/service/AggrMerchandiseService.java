package com.github.liyibo1110.saleproduct.admin.service;

import com.github.liyibo1110.saleproduct.admin.api.dto.MerchandiseChangeMessage;
import com.github.liyibo1110.saleproduct.admin.api.request.MerchandiseCreateRequest;
import com.github.liyibo1110.saleproduct.admin.api.request.MerchandiseUpdateRequest;
import com.github.liyibo1110.saleproduct.admin.api.request.SizeSaveRequest;
import com.github.liyibo1110.saleproduct.admin.entity.Merchandise;
import com.github.liyibo1110.saleproduct.admin.entity.MerchandiseSize;
import com.github.liyibo1110.saleproduct.admin.enums.MerchandiseStatus;
import com.github.liyibo1110.saleproduct.base.exception.Preconditions;
import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聚合商品服务。
 * 在基础CRUD之上封装业务逻辑：事务管理、多表联动、MQ消息发送。
 * 基础Service只负责单表操作，不涉及跨表事务和消息发送。
 * @author liyibo
 * @date 2026-07-13 10:35
 */
@Service
public class AggrMerchandiseService {

    private static final Logger log = LoggerFactory.getLogger(AggrMerchandiseService.class);

    private static final String TOPIC = "merchandise-change-topic";

    private final MerchandiseService merchandiseService;
    private final MerchandiseSizeService sizeService;
    private final MerchandiseAttributeValueService merchandiseAttrValueService;
    private final MerchandiseSizeAttributeValueService sizeAttrValueService;
    private final RocketMQTemplate rocketMQTemplate;

    public AggrMerchandiseService(MerchandiseService merchandiseService,
                                  MerchandiseSizeService sizeService,
                                  MerchandiseAttributeValueService merchandiseAttrValueService,
                                  MerchandiseSizeAttributeValueService sizeAttrValueService,
                                  RocketMQTemplate rocketMQTemplate) {
        this.merchandiseService = merchandiseService;
        this.sizeService = sizeService;
        this.merchandiseAttrValueService = merchandiseAttrValueService;
        this.sizeAttrValueService = sizeAttrValueService;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * 创建商品：商品主体 + 尺码列表 + 动态属性，在一个事务里完成。
     * MQ消息在事务提交后发送，避免发出数据未落库的消息。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createMerchandise(MerchandiseCreateRequest request) {
        Preconditions.checkNotBlank(request.getMerchandiseName(), "商品名称不能为空");
        Preconditions.checkNotBlank(request.getMerchandiseCode(), "商品编码不能为空");

        // 保存商品主体
        Merchandise merchandise = new Merchandise();
        merchandise.setMerchandiseName(request.getMerchandiseName());
        merchandise.setMerchandiseCode(request.getMerchandiseCode());
        merchandise.setStatus(MerchandiseStatus.OFF_SHELF.getCode());
        merchandise.setCategoryId(request.getCategoryId());
        merchandise.setBrandId(request.getBrandId());
        merchandise.setMainImage(request.getMainImage());
        Long merchandiseId = merchandiseService.create(merchandise);

        // 保存尺码列表（含尺码维度属性）
        if (request.getSizeList() != null && !request.getSizeList().isEmpty()) {
            List<MerchandiseSize> sizeList = buildSizeList(request.getSizeList());
            sizeService.batchCreate(merchandiseId, sizeList);

            // 保存尺码维度的动态属性
            for (int i = 0; i < request.getSizeList().size(); i++) {
                MerchandiseCreateRequest.SizeItem item = request.getSizeList().get(i);
                if (item.getSizeAttributes() != null && !item.getSizeAttributes().isEmpty()) {
                    sizeAttrValueService.saveAttributes(sizeList.get(i).getId(), item.getSizeAttributes());
                }
            }
        }

        // 保存商品维度的动态属性
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            merchandiseAttrValueService.saveAttributes(merchandiseId, request.getAttributes());
        }

        // 事务提交后发送MQ
        registerAfterCommit(merchandiseId, "CREATE");

        return merchandiseId;
    }

    /**
     * 更新商品基础信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMerchandise(MerchandiseUpdateRequest request) {
        Preconditions.checkNotNull(request.getMerchandiseId(), "商品ID不能为空");

        Merchandise merchandise = new Merchandise();
        merchandise.setId(request.getMerchandiseId());
        merchandise.setMerchandiseName(request.getMerchandiseName());
        merchandise.setStatus(request.getStatus());
        merchandise.setCategoryId(request.getCategoryId());
        merchandise.setBrandId(request.getBrandId());
        merchandise.setMainImage(request.getMainImage());
        merchandiseService.update(merchandise);

        registerAfterCommit(request.getMerchandiseId(), "UPDATE");
    }

    /**
     * 保存尺码
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveSize(SizeSaveRequest request) {
        Preconditions.checkNotNull(request.getMerchandiseId(), "商品ID不能为空");
        Preconditions.checkNotBlank(request.getSizeCode(), "尺码编码不能为空");

        MerchandiseSize size = new MerchandiseSize();
        size.setMerchandiseId(request.getMerchandiseId());
        size.setSizeCode(request.getSizeCode());
        size.setSizeName(request.getSizeName());
        size.setPrice(request.getPrice());
        size.setOriginalPrice(request.getOriginalPrice());
        size.setStatus(request.getStatus());
        size.setSpecData(request.getSpecData());
        size.setSortOrder(request.getSortOrder());
        sizeService.create(size);

        registerAfterCommit(request.getMerchandiseId(), "UPDATE");
    }

    private List<MerchandiseSize> buildSizeList(List<MerchandiseCreateRequest.SizeItem> items) {
        List<MerchandiseSize> sizeList = new ArrayList<>(items.size());
        for (MerchandiseCreateRequest.SizeItem item : items) {
            MerchandiseSize size = new MerchandiseSize();
            size.setSizeCode(item.getSizeCode());
            size.setSizeName(item.getSizeName());
            size.setPrice(item.getPrice());
            size.setOriginalPrice(item.getOriginalPrice());
            size.setSpecData(item.getSpecData());
            size.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : 0);
            size.setStatus(1);
            sizeList.add(size);
        }
        return sizeList;
    }

    /**
     * 在事务提交后发送MQ消息。
     * 数据必须先落库，消息才能发出去。否则消费者拿到消息去查数据库可能查不到。
     */
    private void registerAfterCommit(Long merchandiseId, String changeType) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        sendChangeMessage(merchandiseId, changeType);
                    }
                }
        );
    }

    private void sendChangeMessage(Long merchandiseId, String changeType) {
        MerchandiseChangeMessage message = new MerchandiseChangeMessage();
        message.setMerchandiseId(merchandiseId);
        message.setChangeType(changeType);
        message.setUpdateTime(LocalDateTime.now());
        try {
            rocketMQTemplate.asyncSend(TOPIC, message,
                    new org.apache.rocketmq.client.producer.SendCallback() {
                        @Override
                        public void onSuccess(org.apache.rocketmq.client.producer.SendResult sendResult) {
                            // 发送成功
                        }

                        @Override
                        public void onException(Throwable e) {
                            StructuredLog.warn(log)
                                    .message("商品变更消息发送失败")
                                    .put("merchandiseId", String.valueOf(merchandiseId))
                                    .put("changeType", changeType)
                                    .exception(e)
                                    .log();
                        }
                    });
        } catch (Exception e) {
            // MQ发送失败不影响主流程，只记日志
            StructuredLog.warn(log)
                    .message("商品变更消息发送异常")
                    .put("merchandiseId", String.valueOf(merchandiseId))
                    .exception(e)
                    .log();
        }
    }
}
