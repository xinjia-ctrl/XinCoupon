package com.xinjia.coupon.search.sync;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

@Repository
public class InMemoryCouponTemplateSearchSyncLogRepository implements CouponTemplateSearchSyncLogRepository {

    private final AtomicLong idGenerator = new AtomicLong(13000);
    private final ConcurrentMap<Long, CouponTemplateSearchSyncLog> logs = new ConcurrentHashMap<>();

    @Override
    public CouponTemplateSearchSyncLog save(CouponTemplateSearchSyncLog log) {
        if (log.getId() == null) {
            log.assignId(idGenerator.incrementAndGet());
        }
        logs.put(log.getId(), log);
        return log;
    }

    @Override
    public List<CouponTemplateSearchSyncLog> findUnconsumed(int limit) {
        return logs.values()
                .stream()
                .filter(log -> !log.isConsumed())
                .sorted(Comparator.comparing(CouponTemplateSearchSyncLog::getCreatedAt))
                .limit(limit)
                .toList();
    }
}
