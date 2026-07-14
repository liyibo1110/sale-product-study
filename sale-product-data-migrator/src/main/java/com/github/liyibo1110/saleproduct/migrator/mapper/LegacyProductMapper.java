package com.github.liyibo1110.saleproduct.migrator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.liyibo1110.saleproduct.migrator.entity.LegacyProduct;
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
public interface LegacyProductMapper extends BaseMapper<LegacyProduct> {

    @Select("SELECT * FROM product_legacy WHERE id > #{lastId} ORDER BY id ASC LIMIT #{batchSize}")
    List<LegacyProduct> selectBatchByIdRange(@Param("lastId") long lastId,
                                             @Param("batchSize") int batchSize);

    @Select("SELECT * FROM product_legacy WHERE update_time >= #{startTime} ORDER BY update_time ASC LIMIT #{batchSize}")
    List<LegacyProduct> selectByUpdateTime(@Param("startTime") LocalDateTime startTime,
                                           @Param("batchSize") int batchSize);

    @Select("SELECT COUNT(*) FROM product_legacy")
    long countAll();
}
