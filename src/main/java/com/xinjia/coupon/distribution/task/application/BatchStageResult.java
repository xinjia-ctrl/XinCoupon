package com.xinjia.coupon.distribution.task.application;

import com.xinjia.coupon.user.coupon.infrastructure.StockDeductStatus;

public record BatchStageResult(StockDeductStatus status, long stagedSize) {

    public boolean success() {
        return status == StockDeductStatus.SUCCESS;
    }
}
