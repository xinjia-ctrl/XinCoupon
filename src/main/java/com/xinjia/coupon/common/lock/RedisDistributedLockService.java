package com.xinjia.coupon.common.lock;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.exception.BusinessException;

@Component
@ConditionalOnProperty(name = "xincoupon.lock.type", havingValue = "redis")
public class RedisDistributedLockService implements DistributedLockService {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;

    public RedisDistributedLockService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public <T> T executeWithLock(String lockKey, Duration ttl, Supplier<T> supplier) {
        String key = "lock:" + lockKey;
        String token = UUID.randomUUID().toString();
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(key, token, ttl);
        if (!Boolean.TRUE.equals(locked)) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "业务处理中，请稍后重试");
        }
        try {
            return supplier.get();
        } finally {
            stringRedisTemplate.execute(UNLOCK_SCRIPT, List.of(key), token);
        }
    }
}
