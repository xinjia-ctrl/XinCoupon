package com.xinjia.coupon.support;

import java.util.ArrayList;
import java.util.List;

import com.xinjia.coupon.dispatch.event.application.CouponEventPublisher;
import com.xinjia.coupon.dispatch.event.domain.CouponEvent;

public class RecordingCouponEventPublisher implements CouponEventPublisher {

    private final List<CouponEvent> events = new ArrayList<>();

    @Override
    public void publish(CouponEvent event) {
        events.add(event);
    }

    public List<CouponEvent> events() {
        return events;
    }
}
