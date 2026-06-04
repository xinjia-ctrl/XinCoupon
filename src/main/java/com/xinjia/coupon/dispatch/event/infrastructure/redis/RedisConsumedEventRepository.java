package com.xinjia.coupon.dispatch.event.infrastructure.redis;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.xinjia.coupon.dispatch.event.application.ConsumedEventRepository;
import com.xinjia.coupon.dispatch.event.domain.CouponEvent;

@Repository
@ConditionalOnProperty(name = "xincoupon.mq.idempotent-store", havingValue = "redis")
public class RedisConsumedEventRepository implements ConsumedEventRepository {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisConsumedEventRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean markIfAbsent(CouponEvent event) {
        Boolean marked = stringRedisTemplate.opsForValue()
                .setIfAbsent("mq:consumed:" + event.eventId(), event.eventType(), Duration.ofDays(7));
        return Boolean.TRUE.equals(marked);
    }
}
