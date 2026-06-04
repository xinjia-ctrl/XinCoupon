package com.xinjia.coupon.engine.remind.web;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateCouponTemplateRemindRequest(
        @Positive Long userId,
        @NotNull @Positive Long templateId,
        @NotBlank @Size(max = 32) String remindType,
        @NotNull @Future OffsetDateTime remindAt
) {

    public CreateCouponTemplateRemindRequest withUserId(Long resolvedUserId) {
        return new CreateCouponTemplateRemindRequest(resolvedUserId, templateId, remindType, remindAt);
    }
}
