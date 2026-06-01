package com.xinjia.coupon.admin.campaign.web;

import java.time.OffsetDateTime;

import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.common.enums.CampaignStatus;

public record CouponCampaignView(
        Long id,
        Long templateId,
        Long merchantId,
        String name,
        Integer campaignStock,
        Integer totalStock,
        Integer availableStock,
        Integer receivedCount,
        Integer perUserLimit,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        CampaignStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static CouponCampaignView from(CouponCampaign campaign) {
        return new CouponCampaignView(
                campaign.getId(),
                campaign.getTemplateId(),
                campaign.getMerchantId(),
                campaign.getName(),
                campaign.getCampaignStock(),
                campaign.getTotalStock(),
                campaign.getAvailableStock(),
                campaign.getReceivedCount(),
                campaign.getPerUserLimit(),
                campaign.getStartTime(),
                campaign.getEndTime(),
                campaign.getStatus(),
                campaign.getCreatedAt(),
                campaign.getUpdatedAt()
        );
    }
}
