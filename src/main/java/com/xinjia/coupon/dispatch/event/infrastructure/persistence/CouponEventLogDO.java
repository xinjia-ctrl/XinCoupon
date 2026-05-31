package com.xinjia.coupon.dispatch.event.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinjia.coupon.common.persistence.BaseAuditEntity;

@TableName("coupon_event_log")
public class CouponEventLogDO extends BaseAuditEntity {

    private String eventId;
    private String eventType;
    private String bizId;
    private String payload;
    private String consumeStatus;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getConsumeStatus() {
        return consumeStatus;
    }

    public void setConsumeStatus(String consumeStatus) {
        this.consumeStatus = consumeStatus;
    }
}
