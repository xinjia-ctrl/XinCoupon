package com.xinjia.coupon.dispatch.event.application;

import com.xinjia.coupon.dispatch.event.domain.CouponEvent;

public interface CouponEventPublisher {

    void publish(CouponEvent event);
}
