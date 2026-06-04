package com.xinjia.coupon.user.coupon.application;

import com.xinjia.coupon.user.coupon.domain.CouponReceiveRequestedEvent;

public interface CouponReceiveRequestPublisher {

    void publish(CouponReceiveRequestedEvent event);
}
