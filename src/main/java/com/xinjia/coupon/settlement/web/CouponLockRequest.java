package com.xinjia.coupon.settlement.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CouponLockRequest(
        @Positive Long userId,
        @NotNull @Positive Long userCouponId,
        @NotBlank @Size(max = 64) String orderNo
) {

    public CouponLockRequest withUserId(Long resolvedUserId) {
        return new CouponLockRequest(resolvedUserId, userCouponId, orderNo);
    }
}
