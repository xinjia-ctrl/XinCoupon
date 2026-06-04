package com.xinjia.coupon.common.idempotent;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(DuplicateSubmitStore.class)
public class InMemoryDuplicateSubmitStore implements DuplicateSubmitStore {

    private final ConcurrentMap<String, Instant> keys = new ConcurrentHashMap<>();

    @Override
    public boolean markIfAbsent(String key, Duration ttl) {
        Instant now = Instant.now();
        keys.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
        Instant expiredAt = now.plus(ttl);
        return keys.putIfAbsent(key, expiredAt) == null;
    }
}
