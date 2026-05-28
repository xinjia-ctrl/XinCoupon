package com.xinjia.coupon.user.coupon.web;

import java.time.OffsetDateTime;

import com.xinjia.coupon.common.enums.UserCouponStatus;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;

public record UserCouponView(
        Long id,
        Long userId,
        Long templateId,
        Long campaignId,
        String couponCode,
        UserCouponStatus status,
        OffsetDateTime receivedAt,
        OffsetDateTime expiredAt
) {

    public static UserCouponView from(UserCoupon userCoupon) {
        return new UserCouponView(
                userCoupon.getId(),
                userCoupon.getUserId(),
                userCoupon.getTemplateId(),
                userCoupon.getCampaignId(),
                userCoupon.getCouponCode(),
                userCoupon.getStatus(),
                userCoupon.getReceivedAt(),
                userCoupon.getExpiredAt()
        );
    }
}
