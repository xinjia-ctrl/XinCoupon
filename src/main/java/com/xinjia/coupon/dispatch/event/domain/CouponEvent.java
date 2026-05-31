package com.xinjia.coupon.dispatch.event.domain;

import java.time.OffsetDateTime;

public interface CouponEvent {

    String eventId();

    String eventType();

    OffsetDateTime occurredAt();
}
