package com.xinjia.coupon.common.idempotent;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class InMemoryDuplicateSubmitStoreTests {

    @Test
    void markIfAbsentShouldRejectDuplicateKeyBeforeExpired() {
        InMemoryDuplicateSubmitStore store = new InMemoryDuplicateSubmitStore();

        assertThat(store.markIfAbsent("key-1", Duration.ofSeconds(5))).isTrue();
        assertThat(store.markIfAbsent("key-1", Duration.ofSeconds(5))).isFalse();
    }
}
