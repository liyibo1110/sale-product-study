package com.github.liyibo1110.saleproduct.migrator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.liyibo1110.saleproduct.migrator.entity.LegacySku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author liyibo
 * @date 2026-07-13 14:49
 */
@Mapper
public interface LegacySkuMapper extends BaseMapper<LegacySku> {

    @Select("SELECT * FROM product_sku_legacy WHERE product_id = #{productId} ORDER BY sort_order ASC")
    List<LegacySku> selectByProductId(@Param("productId") long productId);

    @Select("SELECT * FROM product_sku_legacy WHERE id > #{lastId} ORDER BY id ASC LIMIT #{batchSize}")
    List<LegacySku> selectBatchByIdRange(@Param("lastId") long lastId,
                                         @Param("batchSize") int batchSize);

    @Select("SELECT * FROM product_sku_legacy WHERE update_time >= #{startTime} ORDER BY update_time ASC LIMIT #{batchSize}")
    List<LegacySku> selectByUpdateTime(@Param("startTime") LocalDateTime startTime,
                                       @Param("batchSize") int batchSize);
}
