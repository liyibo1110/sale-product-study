package com.github.liyibo1110.api.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author liyibo
 * @date 2026-07-23 13:29
 */
@Mapper
public interface DescriptionReadMapper {

    @Select("SELECT description FROM merchandise_description " +
            "WHERE merchandise_id = #{merchandiseId} AND is_deleted = 0")
    String selectDescription(@Param("merchandiseId") Long merchandiseId);
}
