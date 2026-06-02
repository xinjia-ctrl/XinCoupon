package com.xinjia.coupon.dispatch.event.infrastructure;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.xinjia.coupon.dispatch.event.application.ConsumedEventRepository;
import com.xinjia.coupon.dispatch.event.domain.CouponEvent;

public class InMemoryConsumedEventRepository implements ConsumedEventRepository {

    private final Set<String> consumedEventIds = ConcurrentHashMap.newKeySet();

    @Override
    public boolean markIfAbsent(CouponEvent event) {
        return consumedEventIds.add(event.eventId());
    }

    public int processedCount() {
        return consumedEventIds.size();
    }
}
