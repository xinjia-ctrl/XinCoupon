package com.xinjia.coupon.settlement.domain;

import java.time.OffsetDateTime;

import com.xinjia.coupon.common.enums.CouponSettlementStatus;

public class CouponSettlement {

    private Long id;
    private Long userId;
    private Long userCouponId;
    private String orderNo;
    private CouponSettlementStatus status;
    private OffsetDateTime lockedAt;
    private OffsetDateTime paidAt;
    private OffsetDateTime canceledAt;
    private OffsetDateTime refundedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private CouponSettlement() {
    }

    public static CouponSettlement lock(Long userId, Long userCouponId, String orderNo) {
        OffsetDateTime now = OffsetDateTime.now();
        CouponSettlement settlement = new CouponSettlement();
        settlement.userId = userId;
        settlement.userCouponId = userCouponId;
        settlement.orderNo = orderNo;
        settlement.status = CouponSettlementStatus.LOCKED;
        settlement.lockedAt = now;
        settlement.createdAt = now;
        settlement.updatedAt = now;
        return settlement;
    }

    public static CouponSettlement restore(
            Long id,
            Long userId,
            Long userCouponId,
            String orderNo,
            CouponSettlementStatus status,
            OffsetDateTime lockedAt,
            OffsetDateTime paidAt,
            OffsetDateTime canceledAt,
            OffsetDateTime refundedAt,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        CouponSettlement settlement = new CouponSettlement();
        settlement.id = id;
        settlement.userId = userId;
        settlement.userCouponId = userCouponId;
        settlement.orderNo = orderNo;
        settlement.status = status;
        settlement.lockedAt = lockedAt;
        settlement.paidAt = paidAt;
        settlement.canceledAt = canceledAt;
        settlement.refundedAt = refundedAt;
        settlement.createdAt = createdAt;
        settlement.updatedAt = updatedAt;
        return settlement;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void markPaid() {
        this.status = CouponSettlementStatus.PAID;
        this.paidAt = OffsetDateTime.now();
        this.updatedAt = this.paidAt;
    }

    public void cancel() {
        this.status = CouponSettlementStatus.CANCELED;
        this.canceledAt = OffsetDateTime.now();
        this.updatedAt = this.canceledAt;
    }

    public void refund() {
        this.status = CouponSettlementStatus.REFUNDED;
        this.refundedAt = OffsetDateTime.now();
        this.updatedAt = this.refundedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getUserCouponId() {
        return userCouponId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public CouponSettlementStatus getStatus() {
        return status;
    }

    public OffsetDateTime getLockedAt() {
        return lockedAt;
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public OffsetDateTime getCanceledAt() {
        return canceledAt;
    }

    public OffsetDateTime getRefundedAt() {
        return refundedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
