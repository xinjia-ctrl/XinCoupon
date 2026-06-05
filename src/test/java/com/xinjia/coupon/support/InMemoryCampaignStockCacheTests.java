package com.xinjia.coupon.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.xinjia.coupon.user.coupon.infrastructure.StockDeductResult;
import com.xinjia.coupon.user.coupon.infrastructure.StockDeductStatus;

class InMemoryCampaignStockCacheTests {

    @Test
    void shouldRejectWhenUserReceiveLimitExceeded() {
        InMemoryCampaignStockCache cache = new InMemoryCampaignStockCache();
        cache.preheatStock(1L, 10);

        StockDeductResult first = cache.tryDeductStock(1L, 99L, 1);
        StockDeductResult second = cache.tryDeductStock(1L, 99L, 1);

        assertThat(first.success()).isTrue();
        assertThat(first.receiveCount()).isEqualTo(1);
        assertThat(second.status()).isEqualTo(StockDeductStatus.RECEIVE_LIMIT_EXCEEDED);
        assertThat(second.receiveCount()).isEqualTo(1);
        assertThat(cache.getStock(1L)).isEqualTo(9);
    }
}
