package com.github.liyibo1110.api.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author liyibo
 * @date 2026-07-23 13:30
 */
@Mapper
public interface ImageReadMapper {

    @Select("SELECT image_url FROM merchandise_image " +
            "WHERE merchandise_id = #{merchandiseId} AND image_type = 'CAROUSEL' AND is_deleted = 0 " +
            "ORDER BY sort_order")
    List<String> selectCarouselImages(@Param("merchandiseId") Long merchandiseId);

    @Select("SELECT image_url FROM merchandise_image " +
            "WHERE merchandise_id = #{merchandiseId} AND image_type = 'DETAIL' AND is_deleted = 0 " +
            "ORDER BY sort_order")
    List<String> selectDetailImages(@Param("merchandiseId") Long merchandiseId);
}
