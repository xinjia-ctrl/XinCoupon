package com.xinjia.coupon.engine.remind.web;

import java.time.OffsetDateTime;

import com.xinjia.coupon.common.enums.CouponTemplateRemindStatus;
import com.xinjia.coupon.engine.remind.domain.CouponTemplateRemind;

public record CouponTemplateRemindView(
        Long id,
        Long userId,
        Long templateId,
        String remindType,
        OffsetDateTime remindAt,
        CouponTemplateRemindStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static CouponTemplateRemindView from(CouponTemplateRemind remind) {
        return new CouponTemplateRemindView(
                remind.getId(),
                remind.getUserId(),
                remind.getTemplateId(),
                remind.getRemindType(),
                remind.getRemindAt(),
                remind.getStatus(),
                remind.getCreatedAt(),
                remind.getUpdatedAt()
        );
    }
}
