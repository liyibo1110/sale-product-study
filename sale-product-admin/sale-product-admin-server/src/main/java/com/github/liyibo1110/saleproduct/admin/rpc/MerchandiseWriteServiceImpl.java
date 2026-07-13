package com.github.liyibo1110.saleproduct.admin.rpc;

import com.github.liyibo1110.saleproduct.admin.api.MerchandiseWriteService;
import com.github.liyibo1110.saleproduct.admin.api.request.AttributeValueSaveRequest;
import com.github.liyibo1110.saleproduct.admin.api.request.MerchandiseCreateRequest;
import com.github.liyibo1110.saleproduct.admin.api.request.MerchandiseSizeAttributeValueSaveRequest;
import com.github.liyibo1110.saleproduct.admin.api.request.MerchandiseUpdateRequest;
import com.github.liyibo1110.saleproduct.admin.api.request.SizeSaveRequest;
import com.github.liyibo1110.saleproduct.admin.service.AggrMerchandiseService;
import com.github.liyibo1110.saleproduct.admin.service.MerchandiseAttributeValueService;
import com.github.liyibo1110.saleproduct.admin.service.MerchandiseSizeAttributeValueService;
import com.github.liyibo1110.saleproduct.base.result.Result;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 商品写入RPC接口实现
 * @author liyibo
 * @date 2026-07-13 10:43
 */
@DubboService
public class MerchandiseWriteServiceImpl implements MerchandiseWriteService {

    private final AggrMerchandiseService aggrMerchandiseService;
    private final MerchandiseAttributeValueService merchandiseAttrValueService;
    private final MerchandiseSizeAttributeValueService sizeAttrValueService;

    public MerchandiseWriteServiceImpl(AggrMerchandiseService aggrMerchandiseService,
                                       MerchandiseAttributeValueService merchandiseAttrValueService,
                                       MerchandiseSizeAttributeValueService sizeAttrValueService) {
        this.aggrMerchandiseService = aggrMerchandiseService;
        this.merchandiseAttrValueService = merchandiseAttrValueService;
        this.sizeAttrValueService = sizeAttrValueService;
    }

    @Override
    public Result<Long> createMerchandise(MerchandiseCreateRequest request) {
        Long merchandiseId = aggrMerchandiseService.createMerchandise(request);
        return Result.success(merchandiseId);
    }

    @Override
    public Result<Void> updateMerchandise(MerchandiseUpdateRequest request) {
        aggrMerchandiseService.updateMerchandise(request);
        return Result.success();
    }

    @Override
    public Result<Void> saveSize(SizeSaveRequest request) {
        aggrMerchandiseService.saveSize(request);
        return Result.success();
    }

    @Override
    public Result<Void> saveMerchandiseAttribute(AttributeValueSaveRequest request) {
        merchandiseAttrValueService.saveOneAttribute(request.getMerchandiseId(), request.getAttrKey(), request.getAttrValue());
        return Result.success();
    }

    @Override
    public Result<Void> saveMerchandiseSizeAttribute(MerchandiseSizeAttributeValueSaveRequest request) {
        sizeAttrValueService.saveOneAttribute(request.getSizeId(), request.getAttrKey(), request.getAttrValue());
        return Result.success();
    }
}
