package com.xinjia.coupon.dispatch.event.infrastructure.rocketmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.dispatch.event.domain.CouponEvent;
import com.xinjia.coupon.dispatch.event.domain.CouponReceivedEvent;

@Component
public class CouponEventMessageConverter {

    private final ObjectMapper objectMapper;

    public CouponEventMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CouponEventMessage toMessage(CouponEvent event) {
        try {
            return new CouponEventMessage(
                    event.eventId(),
                    event.eventType(),
                    objectMapper.writeValueAsString(event),
                    event.occurredAt()
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("优惠券事件序列化失败", exception);
        }
    }

    public String toMessageBody(CouponEvent event) {
        try {
            return objectMapper.writeValueAsString(toMessage(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("优惠券事件消息序列化失败", exception);
        }
    }

    public CouponEvent fromMessageBody(String messageBody) {
        try {
            CouponEventMessage message = objectMapper.readValue(messageBody, CouponEventMessage.class);
            if (CouponReceivedEvent.TYPE.equals(message.eventType())) {
                return objectMapper.readValue(message.payload(), CouponReceivedEvent.class);
            }
            throw new IllegalArgumentException("未知优惠券事件类型: " + message.eventType());
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("优惠券事件消息反序列化失败", exception);
        }
    }
}
