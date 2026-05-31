package com.xinjia.coupon.dispatch.event.infrastructure;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.xinjia.coupon.dispatch.event.application.ConsumedEventRepository;

@Repository
public class InMemoryConsumedEventRepository implements ConsumedEventRepository {

    private final Set<String> consumedEventIds = ConcurrentHashMap.newKeySet();

    @Override
    public boolean markIfAbsent(String eventId) {
        return consumedEventIds.add(eventId);
    }

    public int processedCount() {
        return consumedEventIds.size();
    }
}
