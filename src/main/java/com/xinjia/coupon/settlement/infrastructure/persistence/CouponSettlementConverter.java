package com.xinjia.coupon.settlement.infrastructure.persistence;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import com.xinjia.coupon.common.enums.CouponSettlementStatus;
import com.xinjia.coupon.settlement.domain.CouponSettlement;

@Component
public class CouponSettlementConverter {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    public CouponSettlementDO toDO(CouponSettlement settlement) {
        CouponSettlementDO dataObject = new CouponSettlementDO();
        dataObject.setId(settlement.getId());
        dataObject.setUserId(settlement.getUserId());
        dataObject.setUserCouponId(settlement.getUserCouponId());
        dataObject.setOrderNo(settlement.getOrderNo());
        dataObject.setStatus(settlement.getStatus().name());
        dataObject.setLockedAt(toLocalDateTime(settlement.getLockedAt()));
        dataObject.setPaidAt(toLocalDateTime(settlement.getPaidAt()));
        dataObject.setCanceledAt(toLocalDateTime(settlement.getCanceledAt()));
        dataObject.setRefundedAt(toLocalDateTime(settlement.getRefundedAt()));
        dataObject.setCreatedAt(toLocalDateTime(settlement.getCreatedAt()));
        dataObject.setUpdatedAt(toLocalDateTime(settlement.getUpdatedAt()));
        return dataObject;
    }

    public CouponSettlement toDomain(CouponSettlementDO dataObject) {
        return CouponSettlement.restore(
                dataObject.getId(),
                dataObject.getUserId(),
                dataObject.getUserCouponId(),
                dataObject.getOrderNo(),
                CouponSettlementStatus.valueOf(dataObject.getStatus()),
                toOffsetDateTime(dataObject.getLockedAt()),
                toOffsetDateTime(dataObject.getPaidAt()),
                toOffsetDateTime(dataObject.getCanceledAt()),
                toOffsetDateTime(dataObject.getRefundedAt()),
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
