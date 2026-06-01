package com.xinjia.coupon.admin.template.infrastructure.persistence;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.common.enums.CouponType;

@Component
public class CouponTemplateConverter {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    public CouponTemplateDO toDO(CouponTemplate template) {
        CouponTemplateDO dataObject = new CouponTemplateDO();
        dataObject.setId(template.getId());
        dataObject.setMerchantId(template.getMerchantId());
        dataObject.setTitle(template.getTitle());
        dataObject.setCouponType(template.getCouponType().name());
        dataObject.setDiscountAmount(template.getDiscountAmount());
        dataObject.setDiscountRate(template.getDiscountRate());
        dataObject.setThresholdAmount(template.getThresholdAmount());
        dataObject.setValidStartTime(toLocalDateTime(template.getValidStartTime()));
        dataObject.setValidEndTime(toLocalDateTime(template.getValidEndTime()));
        dataObject.setTotalStock(template.getTotalStock());
        dataObject.setStatus(template.getStatus().name());
        dataObject.setCreatedAt(toLocalDateTime(template.getCreatedAt()));
        dataObject.setUpdatedAt(toLocalDateTime(template.getUpdatedAt()));
        return dataObject;
    }

    public CouponTemplate toDomain(CouponTemplateDO dataObject) {
        return CouponTemplate.restore(
                dataObject.getId(),
                dataObject.getMerchantId(),
                dataObject.getTitle(),
                CouponType.valueOf(dataObject.getCouponType()),
                dataObject.getDiscountAmount(),
                dataObject.getDiscountRate(),
                dataObject.getThresholdAmount(),
                toOffsetDateTime(dataObject.getValidStartTime()),
                toOffsetDateTime(dataObject.getValidEndTime()),
                dataObject.getTotalStock(),
                CouponTemplateStatus.valueOf(dataObject.getStatus()),
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
