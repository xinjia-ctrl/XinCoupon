package com.xinjia.coupon.admin.campaign.domain;

import java.time.OffsetDateTime;

import com.xinjia.coupon.common.enums.CampaignStatus;

public class CouponCampaign {

    private Long id;
    private Long templateId;
    private Long merchantId;
    private String name;
    private Integer totalStock;
    private Integer availableStock;
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
        campaign.totalStock = campaignStock;
        campaign.availableStock = campaignStock;
        campaign.receivedCount = 0;
        campaign.perUserLimit = perUserLimit;
        campaign.startTime = startTime;
        campaign.endTime = endTime;
        campaign.status = CampaignStatus.PENDING;
        campaign.createdAt = now;
        campaign.updatedAt = now;
        return campaign;
    }

    public static CouponCampaign restore(
            Long id,
            Long templateId,
            Long merchantId,
            String name,
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
        CouponCampaign campaign = new CouponCampaign();
        campaign.id = id;
        campaign.templateId = templateId;
        campaign.merchantId = merchantId;
        campaign.name = name;
        campaign.totalStock = totalStock;
        campaign.availableStock = availableStock;
        campaign.receivedCount = receivedCount;
        campaign.perUserLimit = perUserLimit;
        campaign.startTime = startTime;
        campaign.endTime = endTime;
        campaign.status = status;
        campaign.createdAt = createdAt;
        campaign.updatedAt = updatedAt;
        return campaign;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void changeStatus(CampaignStatus status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean deductStock() {
        if (availableStock == null || availableStock <= 0) {
            return false;
        }
        availableStock -= 1;
        receivedCount += 1;
        updatedAt = OffsetDateTime.now();
        return true;
    }

    public void restoreStock() {
        availableStock += 1;
        if (receivedCount > 0) {
            receivedCount -= 1;
        }
        updatedAt = OffsetDateTime.now();
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
        return totalStock;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public Integer getAvailableStock() {
        return availableStock;
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
