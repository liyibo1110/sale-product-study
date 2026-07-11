package com.github.liyibo1110.saleproduct.admin.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品变更消息体
 * @author liyibo
 * @date 2026-07-10 14:41
 */
@Data
public class MerchandiseChangeMessage implements Serializable {

    private Long merchandiseId;

    /** 商品编码 */
    private String merchandiseCode;

    /** 变更类型：CREATE / UPDATE / DELETE */
    private String changeType;

    /** 主库写入时间，用于主从同步校验 */
    private LocalDateTime updateTime;

    /** 变更的维度列表，为空表示全量刷新 */
    private List<String> changedOptions;
}
