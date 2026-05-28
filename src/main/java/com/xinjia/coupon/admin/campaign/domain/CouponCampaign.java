package com.xinjia.coupon.admin.campaign.domain;

import java.time.OffsetDateTime;

import com.xinjia.coupon.common.enums.CampaignStatus;

public class CouponCampaign {

    private Long id;
    private Long templateId;
    private Long merchantId;
    private String name;
    private Integer campaignStock;
    private Integer receivedCount;
    private Integer perUserLimit;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private CampaignStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private CouponCampaign() {
    }

    public static CouponCampaign create(
            Long templateId,
            Long merchantId,
            String name,
            Integer campaignStock,
            Integer perUserLimit,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        CouponCampaign campaign = new CouponCampaign();
        campaign.templateId = templateId;
        campaign.merchantId = merchantId;
        campaign.name = name;
        campaign.campaignStock = campaignStock;
        campaign.receivedCount = 0;
        campaign.perUserLimit = perUserLimit;
        campaign.startTime = startTime;
        campaign.endTime = endTime;
        campaign.status = CampaignStatus.PENDING;
        campaign.createdAt = now;
        campaign.updatedAt = now;
        return campaign;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void changeStatus(CampaignStatus status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public String getName() {
        return name;
    }

    public Integer getCampaignStock() {
        return campaignStock;
    }

    public Integer getReceivedCount() {
        return receivedCount;
    }

    public Integer getPerUserLimit() {
        return perUserLimit;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
