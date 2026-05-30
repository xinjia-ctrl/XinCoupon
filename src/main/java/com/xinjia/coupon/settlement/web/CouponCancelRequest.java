package com.xinjia.coupon.settlement.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CouponCancelRequest(
        @NotNull @Positive Long userId,
        @NotNull @Positive Long userCouponId,
        @NotBlank @Size(max = 64) String orderNo
) {
}
