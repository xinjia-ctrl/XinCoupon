package com.xinjia.coupon.admin.template.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IncreaseCouponTemplateStockRequest(
        @NotNull @Positive Integer increasedStock
) {
}
