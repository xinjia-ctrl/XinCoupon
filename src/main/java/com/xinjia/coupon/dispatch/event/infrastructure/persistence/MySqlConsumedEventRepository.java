package com.xinjia.coupon.dispatch.event.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import com.xinjia.coupon.dispatch.event.application.ConsumedEventRepository;
import com.xinjia.coupon.dispatch.event.domain.CouponEvent;
import com.xinjia.coupon.dispatch.event.domain.CouponReceivedEvent;

@Repository
@ConditionalOnProperty(name = "xincoupon.mq.idempotent-store", havingValue = "mysql", matchIfMissing = true)
public class MySqlConsumedEventRepository implements ConsumedEventRepository {

    private static final String CONSUMED = "CONSUMED";

    private final CouponEventLogMapper couponEventLogMapper;
    private final ObjectMapper objectMapper;

    public MySqlConsumedEventRepository(CouponEventLogMapper couponEventLogMapper, ObjectMapper objectMapper) {
        this.couponEventLogMapper = couponEventLogMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean markIfAbsent(CouponEvent event) {
        CouponEventLogDO dataObject = new CouponEventLogDO();
        dataObject.setEventId(event.eventId());
        dataObject.setEventType(event.eventType());
        dataObject.setBizId(resolveBizId(event));
        dataObject.setPayload(toPayload(event));
        dataObject.setConsumeStatus(CONSUMED);
        try {
            couponEventLogMapper.insert(dataObject);
            return true;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }

    private String resolveBizId(CouponEvent event) {
        if (event instanceof CouponReceivedEvent receivedEvent) {
            return String.valueOf(receivedEvent.userCouponId());
        }
        return event.eventId();
    }

    private String toPayload(CouponEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("优惠券消费事件序列化失败", exception);
        }
    }
}
