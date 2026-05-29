package com.xinjia.coupon.settlement.web;

import java.time.OffsetDateTime;

import com.xinjia.coupon.common.enums.CouponType;

public record AvailableCouponView(
        Long userCouponId,
        Long templateId,
        String couponCode,
        String title,
        CouponType couponType,
        Long thresholdAmount,
        Long discountAmount,
        Integer discountRate,
        Long calculatedDiscountAmount,
        OffsetDateTime expiredAt
) {
}
