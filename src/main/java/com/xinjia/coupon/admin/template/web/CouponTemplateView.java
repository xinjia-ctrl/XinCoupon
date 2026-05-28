package com.xinjia.coupon.admin.template.web;

import java.time.OffsetDateTime;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.common.enums.CouponType;

public record CouponTemplateView(
        Long id,
        Long merchantId,
        String title,
        CouponType couponType,
        Long discountAmount,
        Integer discountRate,
        Long thresholdAmount,
        OffsetDateTime validStartTime,
        OffsetDateTime validEndTime,
        Integer totalStock,
        CouponTemplateStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static CouponTemplateView from(CouponTemplate template) {
        return new CouponTemplateView(
                template.getId(),
                template.getMerchantId(),
                template.getTitle(),
                template.getCouponType(),
                template.getDiscountAmount(),
                template.getDiscountRate(),
                template.getThresholdAmount(),
                template.getValidStartTime(),
                template.getValidEndTime(),
                template.getTotalStock(),
                template.getStatus(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
