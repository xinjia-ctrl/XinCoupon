package com.xinjia.coupon.engine.remind.domain;

import java.time.OffsetDateTime;

import com.xinjia.coupon.common.enums.CouponTemplateRemindStatus;

public class CouponTemplateRemind {

    private Long id;
    private Long userId;
    private Long templateId;
    private String remindType;
    private OffsetDateTime remindAt;
    private CouponTemplateRemindStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private CouponTemplateRemind() {
    }

    public static CouponTemplateRemind create(
            Long userId,
            Long templateId,
            String remindType,
            OffsetDateTime remindAt
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        CouponTemplateRemind remind = new CouponTemplateRemind();
        remind.userId = userId;
        remind.templateId = templateId;
        remind.remindType = remindType;
        remind.remindAt = remindAt;
        remind.status = CouponTemplateRemindStatus.ACTIVE;
        remind.createdAt = now;
        remind.updatedAt = now;
        return remind;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void cancel() {
        this.status = CouponTemplateRemindStatus.CANCELED;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public String getRemindType() {
        return remindType;
    }

    public OffsetDateTime getRemindAt() {
        return remindAt;
    }

    public CouponTemplateRemindStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
