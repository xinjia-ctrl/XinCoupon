package com.xinjia.coupon.user.coupon.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StockDecrementReturnCombinedUtilTests {

    @Test
    void shouldCombineStatusAndReceiveCountIntoSingleLong() {
        long combined = StockDecrementReturnCombinedUtil.combine(StockDeductStatus.RECEIVE_LIMIT_EXCEEDED, 7);

        StockDeductResult result = StockDecrementReturnCombinedUtil.parse(combined);

        assertThat(result.status()).isEqualTo(StockDeductStatus.RECEIVE_LIMIT_EXCEEDED);
        assertThat(result.receiveCount()).isEqualTo(7);
        assertThat(result.success()).isFalse();
    }
}
