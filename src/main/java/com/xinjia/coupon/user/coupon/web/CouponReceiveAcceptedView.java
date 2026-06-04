package com.xinjia.coupon.user.coupon.web;

import java.time.OffsetDateTime;

public record CouponReceiveAcceptedView(
        String eventId,
        String requestId,
        Long userId,
        Long campaignId,
        String status,
        OffsetDateTime acceptedAt
) {
}
