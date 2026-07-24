package com.github.liyibo1110.api.mapper;

import com.github.liyibo1110.api.entity.MerchandiseSize;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-07-23 13:31
 */
@Mapper
public interface MerchandiseSizeReadMapper {

    @Select("SELECT id FROM merchandise WHERE merchandise_code = #{merchandiseCode} AND is_deleted = 0")
    Long selectMerchandiseId(@Param("merchandiseCode") String merchandiseCode);

    @Select("SELECT id, merchandise_id, size_code, size_name, price, original_price, status, spec_data, sort_order " +
            "FROM merchandise_size WHERE merchandise_id = #{merchandiseId} AND is_deleted = 0 ORDER BY sort_order")
    List<MerchandiseSize> selectByMerchandiseId(@Param("merchandiseId") Long merchandiseId);
}
