package com.xinjia.coupon.search.domain;

import java.time.OffsetDateTime;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.common.enums.CouponType;

public record CouponTemplateSearchDocument(
        Long templateId,
        Long merchantId,
        String title,
        CouponType couponType,
        Long thresholdAmount,
        Long discountAmount,
        Integer discountRate,
        CouponTemplateStatus status,
        OffsetDateTime validStartTime,
        OffsetDateTime validEndTime,
        OffsetDateTime updatedAt
) {

    public static CouponTemplateSearchDocument from(CouponTemplate template) {
        return new CouponTemplateSearchDocument(
                template.getId(),
                template.getMerchantId(),
                template.getTitle(),
                template.getCouponType(),
                template.getThresholdAmount(),
                template.getDiscountAmount(),
                template.getDiscountRate(),
                template.getStatus(),
                template.getValidStartTime(),
                template.getValidEndTime(),
                template.getUpdatedAt()
        );
    }
}
