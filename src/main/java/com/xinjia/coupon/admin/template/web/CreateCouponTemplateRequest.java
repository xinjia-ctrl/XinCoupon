package com.xinjia.coupon.admin.template.web;

import java.time.OffsetDateTime;

import com.xinjia.coupon.common.enums.CouponType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateCouponTemplateRequest(
        @NotNull @Positive Long merchantId,
        @NotBlank @Size(max = 80) String title,
        @NotNull CouponType couponType,
        @Positive Long discountAmount,
        @Min(1) @Max(99) Integer discountRate,
        @NotNull @PositiveOrZero Long thresholdAmount,
        @NotNull OffsetDateTime validStartTime,
        @NotNull OffsetDateTime validEndTime,
        @NotNull @Positive Integer totalStock
) {
}
