package com.xinjia.coupon.dispatch.event.infrastructure.rocketmq;

import java.time.OffsetDateTime;

public record CouponEventMessage(
        String eventId,
        String eventType,
        String payload,
        OffsetDateTime occurredAt
) {
}
