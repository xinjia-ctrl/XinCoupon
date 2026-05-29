package com.xinjia.coupon.user.coupon.infrastructure;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.xinjia.coupon.common.config.RedisCacheProperties;

@Repository
public class RedisCampaignStockCache implements CampaignStockCache {

    private static final String CAMPAIGN_STOCK_KEY = "campaign:stock:";

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisCacheProperties redisCacheProperties;

    public RedisCampaignStockCache(
            StringRedisTemplate stringRedisTemplate,
            RedisCacheProperties redisCacheProperties
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisCacheProperties = redisCacheProperties;
    }

    @Override
    public void initializeStock(Long campaignId, Integer stock) {
        String key = buildStockKey(campaignId);
        stringRedisTemplate.opsForValue().setIfAbsent(key, String.valueOf(stock));
    }

    private String buildStockKey(Long campaignId) {
        return redisCacheProperties.buildKey(CAMPAIGN_STOCK_KEY + campaignId);
    }
}
