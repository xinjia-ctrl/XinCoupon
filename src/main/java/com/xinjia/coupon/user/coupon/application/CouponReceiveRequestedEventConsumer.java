package com.xinjia.coupon.user.coupon.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.common.mq.NoMQDuplicateConsume;
import com.xinjia.coupon.user.coupon.domain.CouponReceiveRequestedEvent;
import com.xinjia.coupon.user.coupon.web.ReceiveCouponRequest;

@Component
public class CouponReceiveRequestedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CouponReceiveRequestedEventConsumer.class);

    private final UserCouponService userCouponService;

    public CouponReceiveRequestedEventConsumer(UserCouponService userCouponService) {
        this.userCouponService = userCouponService;
    }

    @EventListener
    @NoMQDuplicateConsume(key = "'coupon-receive-request:' + #event.requestId()")
    public void handle(CouponReceiveRequestedEvent event) {
        userCouponService.receive(new ReceiveCouponRequest(
                event.requestId(),
                event.userId(),
                event.campaignId()
        ));
        log.info("异步领券请求处理完成, eventId={}, requestId={}", event.eventId(), event.requestId());
    }
}
