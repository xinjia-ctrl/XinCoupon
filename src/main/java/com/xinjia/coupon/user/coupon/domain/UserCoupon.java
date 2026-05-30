package com.xinjia.coupon.user.coupon.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.xinjia.coupon.common.enums.UserCouponStatus;

public class UserCoupon {

    private Long id;
    private Long userId;
    private Long templateId;
    private Long campaignId;
    private String couponCode;
    private UserCouponStatus status;
    private OffsetDateTime receivedAt;
    private OffsetDateTime lockedAt;
    private OffsetDateTime usedAt;
    private OffsetDateTime expiredAt;
    private String orderNo;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private UserCoupon() {
    }

    public static UserCoupon receive(Long userId, Long templateId, Long campaignId, OffsetDateTime expiredAt) {
        OffsetDateTime now = OffsetDateTime.now();
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.userId = userId;
        userCoupon.templateId = templateId;
        userCoupon.campaignId = campaignId;
        userCoupon.couponCode = "UC-" + UUID.randomUUID();
        userCoupon.status = UserCouponStatus.RECEIVED;
        userCoupon.receivedAt = now;
        userCoupon.expiredAt = expiredAt;
        userCoupon.createdAt = now;
        userCoupon.updatedAt = now;
        return userCoupon;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void lock(String orderNo) {
        OffsetDateTime now = OffsetDateTime.now();
        this.status = UserCouponStatus.LOCKED;
        this.lockedAt = now;
        this.orderNo = orderNo;
        this.updatedAt = now;
    }

    public void confirmUse() {
        OffsetDateTime now = OffsetDateTime.now();
        this.status = UserCouponStatus.USED;
        this.usedAt = now;
        this.updatedAt = now;
    }

    public void release() {
        this.status = UserCouponStatus.RECEIVED;
        this.lockedAt = null;
        this.orderNo = null;
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

    public Long getCampaignId() {
        return campaignId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public UserCouponStatus getStatus() {
        return status;
    }

    public OffsetDateTime getReceivedAt() {
        return receivedAt;
    }

    public OffsetDateTime getLockedAt() {
        return lockedAt;
    }

    public OffsetDateTime getUsedAt() {
        return usedAt;
    }

    public OffsetDateTime getExpiredAt() {
        return expiredAt;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
