package com.xinjia.coupon.distribution.task.domain;

import java.time.OffsetDateTime;

public class CouponBatchTaskFailure {

    private Long id;
    private Long taskId;
    private String batchNo;
    private Long userId;
    private Integer rowNumber;
    private String failureReason;
    private OffsetDateTime createdAt;

    private CouponBatchTaskFailure() {
    }

    public static CouponBatchTaskFailure create(
            Long taskId,
            String batchNo,
            Long userId,
            Integer rowNumber,
            String failureReason
    ) {
        CouponBatchTaskFailure failure = new CouponBatchTaskFailure();
        failure.taskId = taskId;
        failure.batchNo = batchNo;
        failure.userId = userId;
        failure.rowNumber = rowNumber;
        failure.failureReason = failureReason;
        failure.createdAt = OffsetDateTime.now();
        return failure;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
