package com.xinjia.coupon.settlement.web;

import java.time.OffsetDateTime;

import com.xinjia.coupon.common.enums.UserCouponStatus;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;

public record CouponOperationView(
        Long userCouponId,
        Long userId,
        String couponCode,
        UserCouponStatus status,
        String orderNo,
        OffsetDateTime lockedAt,
        OffsetDateTime usedAt
) {

    public static CouponOperationView from(UserCoupon userCoupon) {
        return new CouponOperationView(
                userCoupon.getId(),
                userCoupon.getUserId(),
                userCoupon.getCouponCode(),
                userCoupon.getStatus(),
                userCoupon.getOrderNo(),
                userCoupon.getLockedAt(),
                userCoupon.getUsedAt()
        );
    }
}
