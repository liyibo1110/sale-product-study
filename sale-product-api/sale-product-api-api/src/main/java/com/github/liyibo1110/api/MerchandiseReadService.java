package com.github.liyibo1110.api;

import com.github.liyibo1110.api.dto.MerchandiseDTO;
import com.github.liyibo1110.api.enums.QueryOption;
import com.github.liyibo1110.saleproduct.base.result.Result;

import java.util.List;
import java.util.Set;

/**
 * 商品读服务Dubbo接口。
 * 提供给所有需要商品数据的调用方：商详页、购物车、下单、搜索等。
 * @author liyibo
 * @date 2026-07-21 10:28
 */
public interface MerchandiseReadService {

    /**
     * 按编码查询单个商品
     * @param merchandiseCode 商品编码
     * @param options         查询选项，为空时默认返回BASE
     * @return 商品数据
     */
    Result<MerchandiseDTO> queryByCode(String merchandiseCode, Set<QueryOption> options);

    /**
     * 批量查询商品
     * @param merchandiseCodes 商品编码列表，上限50个
     * @param options          查询选项
     * @return 商品数据列表（顺序和入参一致，查不到的位置为null）
     */
    Result<List<MerchandiseDTO>> batchQueryByCode(List<String> merchandiseCodes, Set<QueryOption> options);
}
