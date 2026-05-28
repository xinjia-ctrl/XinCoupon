package com.xinjia.coupon.user.coupon.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReceiveCouponRequest(
        @NotNull @Positive Long userId,
        @NotNull @Positive Long campaignId
) {
}
