package com.xinjia.coupon.dispatch.event.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.dispatch.event.application.CouponEventPublisher;
import com.xinjia.coupon.dispatch.event.domain.CouponEvent;

@Component
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "false", matchIfMissing = true)
public class SpringCouponEventPublisher implements CouponEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringCouponEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(CouponEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
