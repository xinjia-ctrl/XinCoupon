package com.xinjia.coupon.dispatch.event.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import com.xinjia.coupon.dispatch.event.domain.CouponReceivedEvent;
import com.xinjia.coupon.dispatch.event.infrastructure.InMemoryConsumedEventRepository;

class CouponReceivedEventConsumerTests {

    @Test
    void handleShouldIgnoreDuplicatedEventId() {
        InMemoryConsumedEventRepository repository = new InMemoryConsumedEventRepository();
        CouponReceivedEventConsumer consumer = new CouponReceivedEventConsumer(repository);
        CouponReceivedEvent event = new CouponReceivedEvent(
                "event-1",
                10L,
                3001L,
                1001L,
                2001L,
                OffsetDateTime.now()
        );

        consumer.handle(event);
        consumer.handle(event);

        assertThat(repository.processedCount()).isEqualTo(1);
    }
}
