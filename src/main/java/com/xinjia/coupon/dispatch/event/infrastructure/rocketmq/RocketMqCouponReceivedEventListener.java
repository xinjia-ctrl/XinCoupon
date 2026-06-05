package com.xinjia.coupon.dispatch.event.infrastructure.rocketmq;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.common.mq.NoMQDuplicateConsume;
import com.xinjia.coupon.dispatch.event.application.CouponReceivedEventConsumer;
import com.xinjia.coupon.dispatch.event.domain.CouponEvent;
import com.xinjia.coupon.dispatch.event.domain.CouponReceivedEvent;

@Component
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = "${xincoupon.dispatch.rocketmq.topic}",
        consumerGroup = "${xincoupon.dispatch.rocketmq.consumer-group}",
        selectorExpression = "${xincoupon.dispatch.rocketmq.received-tag}"
)
public class RocketMqCouponReceivedEventListener implements RocketMQListener<String> {

    private final CouponEventMessageConverter couponEventMessageConverter;
    private final CouponReceivedEventConsumer couponReceivedEventConsumer;

    public RocketMqCouponReceivedEventListener(
            CouponEventMessageConverter couponEventMessageConverter,
            CouponReceivedEventConsumer couponReceivedEventConsumer
    ) {
        this.couponEventMessageConverter = couponEventMessageConverter;
        this.couponReceivedEventConsumer = couponReceivedEventConsumer;
    }

    @Override
    @NoMQDuplicateConsume(key = "'rocketmq:coupon-received:' + #messageBody")
    public void onMessage(String messageBody) {
        CouponEvent event = couponEventMessageConverter.fromMessageBody(messageBody);
        if (event instanceof CouponReceivedEvent receivedEvent) {
            couponReceivedEventConsumer.handle(receivedEvent);
            return;
        }
        throw new IllegalArgumentException("未知领券事件消息: " + event.eventType());
    }
}
