package com.github.liyibo1110.saleproduct.admin.api;

import com.github.liyibo1110.saleproduct.admin.api.request.AttributeValueSaveRequest;
import com.github.liyibo1110.saleproduct.admin.api.request.MerchandiseCreateRequest;
import com.github.liyibo1110.saleproduct.admin.api.request.MerchandiseSizeAttributeValueSaveRequest;
import com.github.liyibo1110.saleproduct.admin.api.request.MerchandiseUpdateRequest;
import com.github.liyibo1110.saleproduct.admin.api.request.SizeSaveRequest;
import com.github.liyibo1110.saleproduct.base.result.Result;

/**
 * 商品写入Dubbo RPC接口。
 * 迁移服务和运营后台只能统一通过这个接口，才能写入商品数据。
 * @author liyibo
 * @date 2026-07-10 12:29
 */
public interface MerchandiseWriteService {

    /**
     * 创建商品（商品主体 + 尺码列表 + 动态属性）
     */
    Result<Long> createMerchandise(MerchandiseCreateRequest request);

    /**
     * 更新商品基础信息
     */
    Result<Void> updateMerchandise(MerchandiseUpdateRequest request);

    /**
     * 保存尺码（编码存在则更新，不存在则新增）
     */
    Result<Void> saveSize(SizeSaveRequest request);

    /**
     * 保存商品维度的属性值
     */
    Result<Void> saveMerchandiseAttribute(AttributeValueSaveRequest request);

    /**
     * 保存尺码维度的属性值
     */
    Result<Void> saveMerchandiseSizeAttribute(MerchandiseSizeAttributeValueSaveRequest request);
}
