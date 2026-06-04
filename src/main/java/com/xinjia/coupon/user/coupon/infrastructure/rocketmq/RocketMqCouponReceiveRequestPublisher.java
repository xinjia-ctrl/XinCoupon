package com.xinjia.coupon.user.coupon.infrastructure.rocketmq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.user.coupon.application.CouponReceiveRequestPublisher;
import com.xinjia.coupon.user.coupon.domain.CouponReceiveRequestedEvent;

@Component
@ConditionalOnProperty(name = "xincoupon.receive.rocketmq.enabled", havingValue = "true")
public class RocketMqCouponReceiveRequestPublisher implements CouponReceiveRequestPublisher {

    private final RocketMQTemplate rocketMQTemplate;
    private final RocketMqCouponReceiveProperties properties;

    public RocketMqCouponReceiveRequestPublisher(
            RocketMQTemplate rocketMQTemplate,
            RocketMqCouponReceiveProperties properties
    ) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(CouponReceiveRequestedEvent event) {
        rocketMQTemplate.convertAndSend(properties.destination(), event);
    }
}
