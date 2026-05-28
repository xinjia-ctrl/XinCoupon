package com.xinjia.coupon.admin.template.domain;

import java.time.OffsetDateTime;

import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.common.enums.CouponType;

public class CouponTemplate {

    private Long id;
    private Long merchantId;
    private String title;
    private CouponType couponType;
    private Long discountAmount;
    private Integer discountRate;
    private Long thresholdAmount;
    private OffsetDateTime validStartTime;
    private OffsetDateTime validEndTime;
    private Integer totalStock;
    private CouponTemplateStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private CouponTemplate() {
    }

    public static CouponTemplate create(
            Long merchantId,
            String title,
            CouponType couponType,
            Long discountAmount,
            Integer discountRate,
            Long thresholdAmount,
            OffsetDateTime validStartTime,
            OffsetDateTime validEndTime,
            Integer totalStock
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        CouponTemplate template = new CouponTemplate();
        template.merchantId = merchantId;
        template.title = title;
        template.couponType = couponType;
        template.discountAmount = discountAmount;
        template.discountRate = discountRate;
        template.thresholdAmount = thresholdAmount;
        template.validStartTime = validStartTime;
        template.validEndTime = validEndTime;
        template.totalStock = totalStock;
        template.status = CouponTemplateStatus.DRAFT;
        template.createdAt = now;
        template.updatedAt = now;
        return template;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void changeStatus(CouponTemplateStatus status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public String getTitle() {
        return title;
    }

    public CouponType getCouponType() {
        return couponType;
    }

    public Long getDiscountAmount() {
        return discountAmount;
    }

    public Integer getDiscountRate() {
        return discountRate;
    }

    public Long getThresholdAmount() {
        return thresholdAmount;
    }

    public OffsetDateTime getValidStartTime() {
        return validStartTime;
    }

    public OffsetDateTime getValidEndTime() {
        return validEndTime;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public CouponTemplateStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
