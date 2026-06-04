package com.xinjia.coupon.user.coupon.infrastructure;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.user.coupon.application.CouponReceiveRequestPublisher;
import com.xinjia.coupon.user.coupon.domain.CouponReceiveRequestedEvent;

@Component
@ConditionalOnMissingBean(CouponReceiveRequestPublisher.class)
public class SpringCouponReceiveRequestPublisher implements CouponReceiveRequestPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringCouponReceiveRequestPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(CouponReceiveRequestedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
