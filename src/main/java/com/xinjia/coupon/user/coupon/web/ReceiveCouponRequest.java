package com.xinjia.coupon.user.coupon.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReceiveCouponRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotNull @Positive Long userId,
        @NotNull @Positive Long campaignId
) {
}
