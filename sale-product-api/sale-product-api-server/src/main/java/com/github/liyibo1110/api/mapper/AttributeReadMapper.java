package com.github.liyibo1110.api.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author liyibo
 * @date 2026-07-23 13:32
 */
@Mapper
public interface AttributeReadMapper {

    @Select("SELECT id FROM merchandise WHERE merchandise_code = #{merchandiseCode} AND is_deleted = 0")
    Long selectMerchandiseId(@Param("merchandiseCode") String merchandiseCode);

    @Select("SELECT ad.attr_key, av.attr_value " +
            "FROM merchandise_attribute_value av " +
            "INNER JOIN attribute_definition ad ON av.attribute_id = ad.id " +
            "WHERE av.merchandise_id = #{merchandiseId} AND ad.status = 1")
    List<Map<String, String>> selectMerchandiseAttributes(@Param("merchandiseId") Long merchandiseId);

    @Select("SELECT ms.size_code, ad.attr_key, sav.attr_value " +
            "FROM merchandise_size_attribute_value sav " +
            "INNER JOIN attribute_definition ad ON sav.attribute_id = ad.id " +
            "INNER JOIN merchandise_size ms ON sav.size_id = ms.id " +
            "WHERE ms.merchandise_id = #{merchandiseId} AND ad.status = 1 AND ms.is_deleted = 0")
    List<Map<String, Object>> selectSizeAttributes(@Param("merchandiseId") Long merchandiseId);
}
