package com.xinjia.coupon.admin.template.web;

import com.xinjia.coupon.common.enums.CouponTemplateStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateCouponTemplateStatusRequest(
        @NotNull CouponTemplateStatus status
) {
}
