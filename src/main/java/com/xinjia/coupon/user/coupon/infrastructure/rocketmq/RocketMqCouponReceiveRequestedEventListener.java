package com.xinjia.coupon.user.coupon.infrastructure.rocketmq;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.common.mq.NoMQDuplicateConsume;
import com.xinjia.coupon.user.coupon.application.UserCouponService;
import com.xinjia.coupon.user.coupon.domain.CouponReceiveRequestedEvent;
import com.xinjia.coupon.user.coupon.web.ReceiveCouponRequest;

@Component
@ConditionalOnProperty(name = "xincoupon.receive.rocketmq.enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = "${xincoupon.receive.rocketmq.topic:xin-coupon-receive}",
        consumerGroup = "${xincoupon.receive.rocketmq.consumer-group:xin-coupon-receive-consumer}"
)
public class RocketMqCouponReceiveRequestedEventListener implements RocketMQListener<CouponReceiveRequestedEvent> {

    private final UserCouponService userCouponService;

    public RocketMqCouponReceiveRequestedEventListener(UserCouponService userCouponService) {
        this.userCouponService = userCouponService;
    }

    @Override
    @NoMQDuplicateConsume(key = "'rocketmq:coupon-receive-request:' + #event.requestId()")
    public void onMessage(CouponReceiveRequestedEvent event) {
        userCouponService.receive(new ReceiveCouponRequest(
                event.requestId(),
                event.userId(),
                event.campaignId()
        ));
    }
}
