package com.xinjia.coupon.dispatch.event.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.dispatch.event.domain.CouponReceivedEvent;

@Component
public class CouponReceivedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CouponReceivedEventConsumer.class);

    private final ConsumedEventRepository consumedEventRepository;

    public CouponReceivedEventConsumer(ConsumedEventRepository consumedEventRepository) {
        this.consumedEventRepository = consumedEventRepository;
    }

    @EventListener
    public void handle(CouponReceivedEvent event) {
        if (!consumedEventRepository.markIfAbsent(event.eventId())) {
            log.info("跳过重复领券事件, eventId={}", event.eventId());
            return;
        }
        log.info(
                "处理领券事件, eventId={}, userId={}, userCouponId={}, campaignId={}",
                event.eventId(),
                event.userId(),
                event.userCouponId(),
                event.campaignId()
        );
    }
}
