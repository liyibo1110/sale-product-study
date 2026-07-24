package com.github.liyibo1110.api.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author liyibo
 * @date 2026-07-23 13:28
 */
@Mapper
public interface CategoryReadMapper {

    @Select("SELECT category_name FROM category WHERE id = #{categoryId} AND is_deleted = 0")
    String selectCategoryName(@Param("categoryId") Long categoryId);

    @Select("SELECT category_path FROM category WHERE id = #{categoryId} AND is_deleted = 0")
    String selectCategoryPath(@Param("categoryId") Long categoryId);
}
