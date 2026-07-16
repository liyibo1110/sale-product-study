package com.github.liyibo1110.saleproduct.check.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 直接操作旧表和新表的查询Mapper。
 * 基础质检需要跨两张表做比对，用原生SQL实现。
 * @author liyibo
 * @date 2026-07-15 16:04
 */
@Mapper
public interface DataCheckMapper {

    @Select("SELECT COUNT(*) FROM product_legacy")
    long countLegacyProducts();

    @Select("SELECT COUNT(*) FROM merchandise WHERE is_deleted = 0")
    long countNewMerchandise();

    @Select("SELECT id FROM product_legacy WHERE id > #{lastId} ORDER BY id ASC LIMIT #{batchSize}")
    List<Long> selectLegacyIdBatch(@Param("lastId") long lastId, @Param("batchSize") int batchSize);

    @Select("SELECT id, product_name, product_code, status, category_id, brand_id, main_image " +
            "FROM product_legacy WHERE id = #{id}")
    Map<String, Object> selectLegacyById(@Param("id") long id);

    @Select("SELECT id, merchandise_name, merchandise_code, status, category_id, brand_id, main_image " +
            "FROM merchandise WHERE merchandise_code = #{merchandiseCode} AND is_deleted = 0")
    Map<String, Object> selectNewByCode(@Param("merchandiseCode") String merchandiseCode);

    @Select("<script>" +
            "SELECT merchandise_code FROM merchandise WHERE merchandise_code IN " +
            "<foreach item='code' collection='codes' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            " AND is_deleted = 0" +
            "</script>")
    List<String> selectExistingCodes(@Param("codes") List<String> codes);

    @Select("SELECT id, product_code FROM product_legacy WHERE id > #{lastId} ORDER BY id ASC LIMIT #{batchSize}")
    List<Map<String, Object>> selectLegacyIdAndCodeBatch(@Param("lastId") long lastId, @Param("batchSize") int batchSize);

    // --- 反向扫描：用于多余检测（新表有、但旧表没有） ---

    @Select("SELECT id, merchandise_code FROM merchandise WHERE id > #{lastId} AND is_deleted = 0 ORDER BY id ASC LIMIT #{batchSize}")
    List<Map<String, Object>> selectNewIdAndCodeBatch(@Param("lastId") long lastId, @Param("batchSize") int batchSize);

    @Select("<script>" +
            "SELECT product_code FROM product_legacy WHERE product_code IN " +
            "<foreach item='code' collection='codes' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            "</script>")
    List<String> selectExistingLegacyCodes(@Param("codes") List<String> codes);

    // --- 增量质检：按更新时间范围扫描 ---

    @Select("SELECT id, product_code FROM product_legacy WHERE update_time >= #{startTime} AND id > #{lastId} ORDER BY id ASC LIMIT #{batchSize}")
    List<Map<String, Object>> selectLegacyIncrementalBatch(@Param("startTime") String startTime, @Param("lastId") long lastId, @Param("batchSize") int batchSize);
}
