package com.xinjia.coupon.user.coupon.infrastructure;

import java.util.List;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.xinjia.coupon.common.config.RedisCacheProperties;

@Repository
public class RedisCampaignStockCache implements CampaignStockCache {

    private static final String CAMPAIGN_STOCK_KEY = "campaign:stock:";
    private static final String CAMPAIGN_RECEIVE_COUNT_KEY = "campaign:receive-count:";
    private static final DefaultRedisScript<Long> DEDUCT_STOCK_SCRIPT = new DefaultRedisScript<>(
            """
            local function combine(status, receiveCount)
                return status * 4294967296 + receiveCount
            end

            local rawStock = redis.call('get', KEYS[1])
            if not rawStock then
                return combine(1, 0)
            end
            local stock = tonumber(rawStock)
            if stock <= 0 then
                return combine(2, 0)
            end
            local perUserLimit = tonumber(ARGV[1])
            local receiveCount = tonumber(redis.call('get', KEYS[2]) or '0')
            if receiveCount >= perUserLimit then
                return combine(3, receiveCount)
            end
            redis.call('decrby', KEYS[1], 1)
            receiveCount = redis.call('incrby', KEYS[2], 1)
            return combine(0, receiveCount)
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
        StockDeductResult result = tryDeductStock(campaignId, 0L, Integer.MAX_VALUE);
        return result.success();
    }

    @Override
    public StockDeductResult tryDeductStock(Long campaignId, Long userId, Integer perUserLimit) {
        Long result = stringRedisTemplate.execute(
                DEDUCT_STOCK_SCRIPT,
                List.of(buildStockKey(campaignId), buildReceiveCountKey(campaignId, userId)),
                String.valueOf(perUserLimit)
        );
        if (result == null) {
            return new StockDeductResult(StockDeductStatus.STOCK_EMPTY, 0);
        }
        return StockDecrementReturnCombinedUtil.parse(result);
    }

    @Override
    public void restoreStock(Long campaignId) {
        stringRedisTemplate.opsForValue().increment(buildStockKey(campaignId));
    }

    @Override
    public void restoreStock(Long campaignId, Long userId) {
        restoreStock(campaignId);
        String receiveCountKey = buildReceiveCountKey(campaignId, userId);
        Long receiveCount = stringRedisTemplate.opsForValue().decrement(receiveCountKey);
        if (receiveCount != null && receiveCount <= 0) {
            stringRedisTemplate.delete(receiveCountKey);
        }
    }

    private String buildStockKey(Long campaignId) {
        return redisCacheProperties.buildKey(CAMPAIGN_STOCK_KEY + campaignId);
    }

    private String buildReceiveCountKey(Long campaignId, Long userId) {
        return redisCacheProperties.buildKey(CAMPAIGN_RECEIVE_COUNT_KEY + campaignId + ":" + userId);
    }
}
