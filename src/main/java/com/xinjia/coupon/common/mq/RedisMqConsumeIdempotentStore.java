package com.xinjia.coupon.common.mq;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "xincoupon.mq.idempotent.store-type", havingValue = "redis")
public class RedisMqConsumeIdempotentStore implements MqConsumeIdempotentStore {

    private static final String KEY_PREFIX = "mq:idempotent:";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisMqConsumeIdempotentStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public MqConsumeMark markConsumingIfAbsent(String key, Duration consumingTtl) {
        String redisKey = buildKey(key);
        Boolean marked = stringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey, MqConsumeState.CONSUMING.name(), consumingTtl);
        if (Boolean.TRUE.equals(marked)) {
            return MqConsumeMark.acquired();
        }
        String value = stringRedisTemplate.opsForValue().get(redisKey);
        MqConsumeState state = value == null ? MqConsumeState.CONSUMING : MqConsumeState.valueOf(value);
        return MqConsumeMark.duplicate(state);
    }

    @Override
    public void markConsumed(String key, Duration consumedTtl) {
        stringRedisTemplate.opsForValue().set(buildKey(key), MqConsumeState.CONSUMED.name(), consumedTtl);
    }

    @Override
    public void markRetryable(String key) {
        stringRedisTemplate.delete(buildKey(key));
    }

    private String buildKey(String key) {
        return KEY_PREFIX + key;
    }
}
