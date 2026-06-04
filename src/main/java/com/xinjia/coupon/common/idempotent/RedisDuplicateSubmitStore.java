package com.xinjia.coupon.common.idempotent;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "xincoupon.idempotent.store-type", havingValue = "redis")
public class RedisDuplicateSubmitStore implements DuplicateSubmitStore {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisDuplicateSubmitStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean markIfAbsent(String key, Duration ttl) {
        Boolean marked = stringRedisTemplate.opsForValue().setIfAbsent("idempotent:" + key, "1", ttl);
        return Boolean.TRUE.equals(marked);
    }
}
