package com.xinjia.coupon.settlement.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record OrderItemRequest(
        @NotBlank @Size(max = 64) String skuId,
        @NotBlank @Size(max = 64) String categoryCode,
        @NotNull @Positive Integer quantity,
        @NotNull @PositiveOrZero Long amount
) {
}
