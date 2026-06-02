package com.xinjia.coupon.dispatch.event.infrastructure.rocketmq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.dispatch.event.application.CouponEventPublisher;
import com.xinjia.coupon.dispatch.event.domain.CouponEvent;
import com.xinjia.coupon.dispatch.event.domain.CouponReceivedEvent;

@Component
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true")
public class RocketMqCouponEventPublisher implements CouponEventPublisher {

    private final RocketMQTemplate rocketMQTemplate;
    private final RocketMqDispatchProperties rocketMqDispatchProperties;
    private final CouponEventMessageConverter couponEventMessageConverter;

    public RocketMqCouponEventPublisher(
            RocketMQTemplate rocketMQTemplate,
            RocketMqDispatchProperties rocketMqDispatchProperties,
            CouponEventMessageConverter couponEventMessageConverter
    ) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.rocketMqDispatchProperties = rocketMqDispatchProperties;
        this.couponEventMessageConverter = couponEventMessageConverter;
    }

    @Override
    public void publish(CouponEvent event) {
        if (CouponReceivedEvent.TYPE.equals(event.eventType())) {
            rocketMQTemplate.convertAndSend(
                    rocketMqDispatchProperties.receivedDestination(),
                    couponEventMessageConverter.toMessageBody(event)
            );
            return;
        }
        throw new IllegalArgumentException("未知优惠券事件类型: " + event.eventType());
    }
}
