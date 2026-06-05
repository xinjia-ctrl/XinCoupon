package com.xinjia.coupon.common.mq;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class InMemoryMqConsumeIdempotentStoreTests {

    @Test
    void shouldKeepConsumingConsumedAndRetryableStates() {
        InMemoryMqConsumeIdempotentStore store = new InMemoryMqConsumeIdempotentStore();

        MqConsumeMark first = store.markConsumingIfAbsent("event-1", Duration.ofSeconds(30));
        MqConsumeMark consumingDuplicate = store.markConsumingIfAbsent("event-1", Duration.ofSeconds(30));
        store.markConsumed("event-1", Duration.ofSeconds(60));
        MqConsumeMark consumedDuplicate = store.markConsumingIfAbsent("event-1", Duration.ofSeconds(30));
        store.markRetryable("event-1");
        MqConsumeMark retry = store.markConsumingIfAbsent("event-1", Duration.ofSeconds(30));

        assertThat(first.accepted()).isTrue();
        assertThat(consumingDuplicate.accepted()).isFalse();
        assertThat(consumingDuplicate.currentState()).isEqualTo(MqConsumeState.CONSUMING);
        assertThat(consumedDuplicate.accepted()).isFalse();
        assertThat(consumedDuplicate.currentState()).isEqualTo(MqConsumeState.CONSUMED);
        assertThat(retry.accepted()).isTrue();
    }
}
