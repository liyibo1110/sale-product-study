package com.github.liyibo1110.api.mapper;

import com.github.liyibo1110.api.entity.Merchandise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * @author liyibo
 * @date 2026-07-23 13:31
 */
@Mapper
public interface MerchandiseReadMapper {

    @Select("SELECT id, merchandise_name, merchandise_code, status, category_id, brand_id, main_image " +
            "FROM merchandise WHERE merchandise_code = #{merchandiseCode} AND is_deleted = 0")
    Merchandise selectByCode(@Param("merchandiseCode") String merchandiseCode);

    @Select("SELECT id FROM merchandise WHERE merchandise_code = #{merchandiseCode} AND is_deleted = 0")
    Long selectIdByCode(@Param("merchandiseCode") String merchandiseCode);

    @Select("SELECT brand_id FROM merchandise WHERE merchandise_code = #{merchandiseCode} AND is_deleted = 0")
    Long selectBrandId(@Param("merchandiseCode") String merchandiseCode);

    @Select("SELECT category_id FROM merchandise WHERE merchandise_code = #{merchandiseCode} AND is_deleted = 0")
    Long selectCategoryId(@Param("merchandiseCode") String merchandiseCode);

    @Select("SELECT update_time FROM merchandise WHERE merchandise_code = #{merchandiseCode} AND is_deleted = 0")
    LocalDateTime selectUpdateTime(@Param("merchandiseCode") String merchandiseCode);
}
