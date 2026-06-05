package com.xinjia.coupon.user.coupon.infrastructure;

public record StockDeductResult(
        StockDeductStatus status,
        long receiveCount
) {

    public boolean success() {
        return status == StockDeductStatus.SUCCESS;
    }
}
