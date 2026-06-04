package com.xinjia.coupon.engine.remind.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CancelCouponTemplateRemindRequest(
        @Positive Long userId,
        @NotNull @Positive Long remindId
) {

    public CancelCouponTemplateRemindRequest withUserId(Long resolvedUserId) {
        return new CancelCouponTemplateRemindRequest(resolvedUserId, remindId);
    }
}
