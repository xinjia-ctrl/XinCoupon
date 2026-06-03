package com.xinjia.coupon.settlement.web;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SettlementCalculateRequest(
        @Positive Long userId,
        @NotBlank @Size(max = 64) String orderNo,
        @NotNull @Positive Long merchantId,
        @NotNull @PositiveOrZero Long orderAmount,
        @Valid @NotEmpty List<OrderItemRequest> items
) {

    public SettlementCalculateRequest withUserId(Long resolvedUserId) {
        return new SettlementCalculateRequest(resolvedUserId, orderNo, merchantId, orderAmount, items);
    }
}
