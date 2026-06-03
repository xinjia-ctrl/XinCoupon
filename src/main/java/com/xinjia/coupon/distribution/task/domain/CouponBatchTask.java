package com.xinjia.coupon.distribution.task.domain;

import java.time.OffsetDateTime;

import com.xinjia.coupon.common.enums.CouponBatchTaskStatus;

public class CouponBatchTask {

    private Long id;
    private String batchNo;
    private Long campaignId;
    private Integer totalCount;
    private Integer successCount;
    private Integer failureCount;
    private CouponBatchTaskStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private CouponBatchTask() {
    }

    public static CouponBatchTask create(String batchNo, Long campaignId, Integer totalCount) {
        OffsetDateTime now = OffsetDateTime.now();
        CouponBatchTask task = new CouponBatchTask();
        task.batchNo = batchNo;
        task.campaignId = campaignId;
        task.totalCount = totalCount;
        task.successCount = 0;
        task.failureCount = 0;
        task.status = CouponBatchTaskStatus.PENDING;
        task.createdAt = now;
        task.updatedAt = now;
        return task;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void markRunning() {
        this.status = CouponBatchTaskStatus.RUNNING;
        this.updatedAt = OffsetDateTime.now();
    }

    public void recordSuccess() {
        this.successCount++;
        this.updatedAt = OffsetDateTime.now();
    }

    public void recordFailure() {
        this.failureCount++;
        this.updatedAt = OffsetDateTime.now();
    }

    public void complete() {
        if (failureCount == 0) {
            this.status = CouponBatchTaskStatus.COMPLETED;
        } else if (successCount == 0) {
            this.status = CouponBatchTaskStatus.FAILED;
        } else {
            this.status = CouponBatchTaskStatus.PARTIAL_FAILED;
        }
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public CouponBatchTaskStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
