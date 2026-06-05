package com.xinjia.coupon.common.mq;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(MqConsumeIdempotentStore.class)
public class InMemoryMqConsumeIdempotentStore implements MqConsumeIdempotentStore {

    private final ConcurrentMap<String, Entry> entries = new ConcurrentHashMap<>();

    @Override
    public synchronized MqConsumeMark markConsumingIfAbsent(String key, Duration consumingTtl) {
        Instant now = Instant.now();
        removeExpired(now);
        Entry current = entries.get(key);
        if (current != null) {
            return MqConsumeMark.duplicate(current.state());
        }
        entries.put(key, new Entry(MqConsumeState.CONSUMING, now.plus(consumingTtl)));
        return MqConsumeMark.acquired();
    }

    @Override
    public void markConsumed(String key, Duration consumedTtl) {
        entries.put(key, new Entry(MqConsumeState.CONSUMED, Instant.now().plus(consumedTtl)));
    }

    @Override
    public void markRetryable(String key) {
        entries.remove(key);
    }

    private void removeExpired(Instant now) {
        entries.entrySet().removeIf(entry -> !entry.getValue().expiredAt().isAfter(now));
    }

    private record Entry(MqConsumeState state, Instant expiredAt) {
    }
}
