package com.xinjia.coupon.user.coupon.infrastructure.persistence;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import com.xinjia.coupon.common.enums.UserCouponStatus;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;

@Component
public class UserCouponConverter {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    public UserCouponDO toDO(UserCoupon userCoupon) {
        UserCouponDO dataObject = new UserCouponDO();
        dataObject.setId(userCoupon.getId());
        dataObject.setUserId(userCoupon.getUserId());
        dataObject.setTemplateId(userCoupon.getTemplateId());
        dataObject.setCampaignId(userCoupon.getCampaignId());
        dataObject.setCouponCode(userCoupon.getCouponCode());
        dataObject.setStatus(userCoupon.getStatus().name());
        dataObject.setReceivedAt(toLocalDateTime(userCoupon.getReceivedAt()));
        dataObject.setLockedAt(toLocalDateTime(userCoupon.getLockedAt()));
        dataObject.setUsedAt(toLocalDateTime(userCoupon.getUsedAt()));
        dataObject.setExpiredAt(toLocalDateTime(userCoupon.getExpiredAt()));
        dataObject.setOrderNo(userCoupon.getOrderNo());
        dataObject.setCreatedAt(toLocalDateTime(userCoupon.getCreatedAt()));
        dataObject.setUpdatedAt(toLocalDateTime(userCoupon.getUpdatedAt()));
        return dataObject;
    }

    public UserCoupon toDomain(UserCouponDO dataObject) {
        return UserCoupon.restore(
                dataObject.getId(),
                dataObject.getUserId(),
                dataObject.getTemplateId(),
                dataObject.getCampaignId(),
                dataObject.getCouponCode(),
                UserCouponStatus.valueOf(dataObject.getStatus()),
                toOffsetDateTime(dataObject.getReceivedAt()),
                toOffsetDateTime(dataObject.getLockedAt()),
                toOffsetDateTime(dataObject.getUsedAt()),
                toOffsetDateTime(dataObject.getExpiredAt()),
                dataObject.getOrderNo(),
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
