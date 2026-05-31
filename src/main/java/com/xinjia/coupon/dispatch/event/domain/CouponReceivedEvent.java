package com.xinjia.coupon.dispatch.event.domain;

import java.time.OffsetDateTime;

public record CouponReceivedEvent(
        String eventId,
        Long userId,
        Long userCouponId,
        Long templateId,
        Long campaignId,
        OffsetDateTime occurredAt
) implements CouponEvent {

    public static final String TYPE = "COUPON_RECEIVED";

    @Override
    public String eventType() {
        return TYPE;
    }
}
