package com.xinjia.coupon.distribution.task.web;

import java.time.OffsetDateTime;

import com.xinjia.coupon.common.enums.CouponBatchTaskStatus;
import com.xinjia.coupon.distribution.task.domain.CouponBatchTask;

public record CouponBatchTaskView(
        Long id,
        String batchNo,
        Long campaignId,
        Integer totalCount,
        Integer successCount,
        Integer failureCount,
        CouponBatchTaskStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static CouponBatchTaskView from(CouponBatchTask task) {
        return new CouponBatchTaskView(
                task.getId(),
                task.getBatchNo(),
                task.getCampaignId(),
                task.getTotalCount(),
                task.getSuccessCount(),
                task.getFailureCount(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
