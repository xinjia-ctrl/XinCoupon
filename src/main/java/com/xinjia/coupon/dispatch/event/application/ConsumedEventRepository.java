package com.xinjia.coupon.dispatch.event.application;

public interface ConsumedEventRepository {

    boolean markIfAbsent(String eventId);
}
