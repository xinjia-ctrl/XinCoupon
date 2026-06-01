package com.xinjia.coupon.user.coupon.infrastructure;

import java.util.List;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.xinjia.coupon.common.config.RedisCacheProperties;

@Repository
public class RedisCampaignStockCache implements CampaignStockCache {

    private static final String CAMPAIGN_STOCK_KEY = "campaign:stock:";
    private static final DefaultRedisScript<Long> DEDUCT_STOCK_SCRIPT = new DefaultRedisScript<>(
            """
            local rawStock = redis.call('get', KEYS[1])
            if not rawStock then
                return -1
            end
            local stock = tonumber(rawStock)
            if stock <= 0 then
                return 0
            end
            return redis.call('decrby', KEYS[1], 1)
            """,
            Long.class
    );

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

    @Override
    public void preheatStock(Long campaignId, Integer stock) {
        String key = buildStockKey(campaignId);
        stringRedisTemplate.opsForValue().set(key, String.valueOf(stock));
    }

    @Override
    public boolean tryDeductStock(Long campaignId) {
        Long result = stringRedisTemplate.execute(DEDUCT_STOCK_SCRIPT, List.of(buildStockKey(campaignId)));
        return result != null && result >= 0;
    }

    @Override
    public void restoreStock(Long campaignId) {
        stringRedisTemplate.opsForValue().increment(buildStockKey(campaignId));
    }

    private String buildStockKey(Long campaignId) {
        return redisCacheProperties.buildKey(CAMPAIGN_STOCK_KEY + campaignId);
    }
}
