package com.xinjia.coupon.admin.campaign.web;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateCouponCampaignRequest(
        @NotNull @Positive Long templateId,
        @NotNull @Positive Long merchantId,
        @NotBlank @Size(max = 80) String name,
        @NotNull @Positive Integer campaignStock,
        @NotNull @Positive Integer perUserLimit,
        @NotNull OffsetDateTime startTime,
        @NotNull OffsetDateTime endTime
) {
}
