package com.xinjia.coupon.user.coupon.domain;

import java.time.OffsetDateTime;

public record CouponReceiveRequestedEvent(
        String eventId,
        String requestId,
        Long userId,
        Long campaignId,
        OffsetDateTime requestedAt
) {
}
