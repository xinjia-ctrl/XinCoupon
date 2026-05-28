package com.xinjia.coupon.admin.campaign.web;

import com.xinjia.coupon.common.enums.CampaignStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateCouponCampaignStatusRequest(
        @NotNull CampaignStatus status
) {
}
