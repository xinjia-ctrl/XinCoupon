package com.xinjia.coupon.distribution.task.web;

import java.time.OffsetDateTime;

import com.xinjia.coupon.distribution.task.domain.CouponBatchTaskFailure;

public record CouponBatchTaskFailureView(
        Long id,
        Long taskId,
        String batchNo,
        Long userId,
        Integer rowNumber,
        String failureReason,
        OffsetDateTime createdAt
) {

    public static CouponBatchTaskFailureView from(CouponBatchTaskFailure failure) {
        return new CouponBatchTaskFailureView(
                failure.getId(),
                failure.getTaskId(),
                failure.getBatchNo(),
                failure.getUserId(),
                failure.getRowNumber(),
                failure.getFailureReason(),
                failure.getCreatedAt()
        );
    }
}
