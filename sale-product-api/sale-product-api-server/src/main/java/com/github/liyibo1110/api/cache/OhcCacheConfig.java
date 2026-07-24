package com.github.liyibo1110.api.cache;

import com.github.liyibo1110.api.loader.data.AttributeData;
import com.github.liyibo1110.api.loader.data.BaseMerchandiseData;
import com.github.liyibo1110.api.loader.data.BrandData;
import com.github.liyibo1110.api.loader.data.CategoryData;
import com.github.liyibo1110.api.loader.data.DescriptionData;
import com.github.liyibo1110.api.loader.data.ImageData;
import com.github.liyibo1110.api.loader.data.PriceData;
import com.github.liyibo1110.api.loader.data.SkuData;
import org.caffinitas.ohc.OHCache;
import org.caffinitas.ohc.OHCacheBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyibo
 * @date 2026-07-23 13:33
 */
@Configuration
public class OhcCacheConfig {

    @Bean
    public OHCache<String, BaseMerchandiseData> baseCache() {
        return OHCacheBuilder.<String, BaseMerchandiseData>newBuilder()
                .keySerializer(new StringKeySerializer())
                .valueSerializer(new KryoValueSerializer<>(BaseMerchandiseData.class))
                // 容量256MB
                .capacity(256L * 1024 * 1024)
                // 哈希表分段数，和CPU核数对齐减少锁竞争
                .segmentCount(64)
                // 启用TTL支持
                .timeouts(true)
                // 条目过期时间5分钟
                .defaultTTLmillis(300_000)
                .build();
    }

    @Bean
    public OHCache<String, SkuData> skuCache() {
        return OHCacheBuilder.<String, SkuData>newBuilder()
                .keySerializer(new StringKeySerializer())
                .valueSerializer(new KryoValueSerializer<>(SkuData.class))
                // SKU数据体积较大，分配512MB
                .capacity(512L * 1024 * 1024)
                .segmentCount(64)
                .timeouts(true)
                .defaultTTLmillis(300_000)
                .build();
    }

    @Bean
    public OHCache<String, AttributeData> attributeCache() {
        return OHCacheBuilder.<String, AttributeData>newBuilder()
                .keySerializer(new StringKeySerializer())
                .valueSerializer(new KryoValueSerializer<>(AttributeData.class))
                .capacity(256L * 1024 * 1024)
                .segmentCount(64)
                .timeouts(true)
                .defaultTTLmillis(300_000)
                .build();
    }

    @Bean
    public OHCache<String, PriceData> priceCache() {
        return OHCacheBuilder.<String, PriceData>newBuilder()
                .keySerializer(new StringKeySerializer())
                .valueSerializer(new KryoValueSerializer<>(PriceData.class))
                .capacity(64L * 1024 * 1024)
                .segmentCount(64)
                .timeouts(true)
                .defaultTTLmillis(300_000)
                .build();
    }

    @Bean
    public OHCache<String, BrandData> brandCache() {
        return OHCacheBuilder.<String, BrandData>newBuilder()
                .keySerializer(new StringKeySerializer())
                .valueSerializer(new KryoValueSerializer<>(BrandData.class))
                .capacity(64L * 1024 * 1024)
                .segmentCount(64)
                .timeouts(true)
                .defaultTTLmillis(300_000)
                .build();
    }

    @Bean
    public OHCache<String, CategoryData> categoryCache() {
        return OHCacheBuilder.<String, CategoryData>newBuilder()
                .keySerializer(new StringKeySerializer())
                .valueSerializer(new KryoValueSerializer<>(CategoryData.class))
                .capacity(64L * 1024 * 1024)
                .segmentCount(64)
                .timeouts(true)
                .defaultTTLmillis(300_000)
                .build();
    }

    @Bean
    public OHCache<String, ImageData> imageCache() {
        return OHCacheBuilder.<String, ImageData>newBuilder()
                .keySerializer(new StringKeySerializer())
                .valueSerializer(new KryoValueSerializer<>(ImageData.class))
                .capacity(128L * 1024 * 1024)
                .segmentCount(64)
                .timeouts(true)
                .defaultTTLmillis(300_000)
                .build();
    }

    @Bean
    public OHCache<String, DescriptionData> descriptionCache() {
        return OHCacheBuilder.<String, DescriptionData>newBuilder()
                .keySerializer(new StringKeySerializer())
                .valueSerializer(new KryoValueSerializer<>(DescriptionData.class))
                // DESCRIPTION单条体积大但访问频率低，配小容量
                .capacity(128L * 1024 * 1024)
                .segmentCount(64)
                .timeouts(true)
                .defaultTTLmillis(300_000)
                .build();
    }
}
