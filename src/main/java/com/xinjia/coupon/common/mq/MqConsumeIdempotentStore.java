package com.xinjia.coupon.common.mq;

import java.time.Duration;

public interface MqConsumeIdempotentStore {

    MqConsumeMark markConsumingIfAbsent(String key, Duration consumingTtl);

    void markConsumed(String key, Duration consumedTtl);

    void markRetryable(String key);
}
