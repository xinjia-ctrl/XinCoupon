package com.xinjia.coupon.admin.campaign.infrastructure.persistence;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.common.enums.CampaignStatus;

@Component
public class CouponCampaignConverter {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    public CouponCampaignDO toDO(CouponCampaign campaign) {
        CouponCampaignDO dataObject = new CouponCampaignDO();
        dataObject.setId(campaign.getId());
        dataObject.setTemplateId(campaign.getTemplateId());
        dataObject.setMerchantId(campaign.getMerchantId());
        dataObject.setName(campaign.getName());
        dataObject.setTotalStock(campaign.getTotalStock());
        dataObject.setAvailableStock(campaign.getAvailableStock());
        dataObject.setReceivedCount(campaign.getReceivedCount());
        dataObject.setPerUserLimit(campaign.getPerUserLimit());
        dataObject.setStartTime(toLocalDateTime(campaign.getStartTime()));
        dataObject.setEndTime(toLocalDateTime(campaign.getEndTime()));
        dataObject.setStatus(campaign.getStatus().name());
        dataObject.setCreatedAt(toLocalDateTime(campaign.getCreatedAt()));
        dataObject.setUpdatedAt(toLocalDateTime(campaign.getUpdatedAt()));
        return dataObject;
    }

    public CouponCampaign toDomain(CouponCampaignDO dataObject) {
        return CouponCampaign.restore(
                dataObject.getId(),
                dataObject.getTemplateId(),
                dataObject.getMerchantId(),
                dataObject.getName(),
                dataObject.getTotalStock(),
                dataObject.getAvailableStock(),
                dataObject.getReceivedCount(),
                dataObject.getPerUserLimit(),
                toOffsetDateTime(dataObject.getStartTime()),
                toOffsetDateTime(dataObject.getEndTime()),
                CampaignStatus.valueOf(dataObject.getStatus()),
                toOffsetDateTime(dataObject.getCreatedAt()),
                toOffsetDateTime(dataObject.getUpdatedAt())
        );
    }

    private LocalDateTime toLocalDateTime(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZoneSameInstant(ZONE_ID).toLocalDateTime();
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(ZONE_ID).toOffsetDateTime();
    }
}
