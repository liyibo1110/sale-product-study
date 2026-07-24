package com.github.liyibo1110.api.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author liyibo
 * @date 2026-07-23 13:28
 */
@Mapper
public interface BrandReadMapper {

    @Select("SELECT brand_name FROM brand WHERE id = #{brandId} AND is_deleted = 0")
    String selectBrandName(@Param("brandId") Long brandId);

    @Select("SELECT brand_logo FROM brand WHERE id = #{brandId} AND is_deleted = 0")
    String selectBrandLogo(@Param("brandId") Long brandId);
}
