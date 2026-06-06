package com.xinjia.coupon.distribution.task.application;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.common.config.RedisCacheProperties;
import com.xinjia.coupon.user.coupon.infrastructure.StockDecrementReturnCombinedUtil;
import com.xinjia.coupon.user.coupon.infrastructure.StockDeductResult;
import com.xinjia.coupon.user.coupon.infrastructure.StockDeductStatus;

@Component
@ConditionalOnProperty(name = "xincoupon.batch.staging.store-type", havingValue = "redis", matchIfMissing = true)
public class RedisCouponBatchUserStagingStore implements CouponBatchUserStagingStore {

    private static final String CAMPAIGN_STOCK_KEY = "campaign:stock:";
    private static final String CAMPAIGN_RECEIVE_COUNT_KEY = "campaign:receive-count:";
    private static final String BATCH_STAGED_USER_KEY = "coupon-batch-task:staged-users:";
    private static final DefaultRedisScript<Long> STAGE_SCRIPT = buildStageScript();

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisCacheProperties redisCacheProperties;

    public RedisCouponBatchUserStagingStore(
            StringRedisTemplate stringRedisTemplate,
            RedisCacheProperties redisCacheProperties
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisCacheProperties = redisCacheProperties;
    }

    @Override
    public BatchStageResult stage(Long taskId, Long campaignId, BatchUserRow row, Integer perUserLimit) {
        Long combined = stringRedisTemplate.execute(
                STAGE_SCRIPT,
                List.of(buildStockKey(campaignId), buildStagedUserKey(taskId), buildReceiveCountKey(campaignId, row.userId())),
                encode(row),
                String.valueOf(perUserLimit)
        );
        if (combined == null) {
            return new BatchStageResult(StockDeductStatus.STOCK_EMPTY, size(taskId));
        }
        StockDeductResult parsed = StockDecrementReturnCombinedUtil.parse(combined);
        return new BatchStageResult(parsed.status(), parsed.receiveCount());
    }

    @Override
    public List<BatchUserRow> pop(Long taskId, int count) {
        List<String> values = stringRedisTemplate.opsForSet().pop(buildStagedUserKey(taskId), count);
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .map(this::decode)
                .toList();
    }

    @Override
    public long size(Long taskId) {
        Long size = stringRedisTemplate.opsForSet().size(buildStagedUserKey(taskId));
        return size == null ? 0 : size;
    }

    @Override
    public void restoreReservation(Long campaignId, Long userId) {
        stringRedisTemplate.opsForValue().increment(buildStockKey(campaignId));
        String receiveCountKey = buildReceiveCountKey(campaignId, userId);
        Long receiveCount = stringRedisTemplate.opsForValue().decrement(receiveCountKey);
        if (receiveCount != null && receiveCount <= 0) {
            stringRedisTemplate.delete(receiveCountKey);
        }
    }

    @Override
    public void clear(Long taskId) {
        stringRedisTemplate.delete(buildStagedUserKey(taskId));
    }

    private String buildStockKey(Long campaignId) {
        return redisCacheProperties.buildKey(CAMPAIGN_STOCK_KEY + campaignId);
    }

    private String buildReceiveCountKey(Long campaignId, Long userId) {
        return redisCacheProperties.buildKey(CAMPAIGN_RECEIVE_COUNT_KEY + campaignId + ":" + userId);
    }

    private String buildStagedUserKey(Long taskId) {
        return redisCacheProperties.buildKey(BATCH_STAGED_USER_KEY + taskId);
    }

    private String encode(BatchUserRow row) {
        return row.userId() + ":" + row.rowNumber();
    }

    private BatchUserRow decode(String value) {
        String[] parts = value.split(":", 2);
        return new BatchUserRow(Long.valueOf(parts[0]), Integer.parseInt(parts[1]));
    }

    private static DefaultRedisScript<Long> buildStageScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/batch_stage_user_coupon.lua")));
        script.setResultType(Long.class);
        return script;
    }
}
