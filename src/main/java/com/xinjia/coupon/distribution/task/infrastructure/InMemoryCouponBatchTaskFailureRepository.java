package com.xinjia.coupon.distribution.task.infrastructure;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.xinjia.coupon.distribution.task.domain.CouponBatchTaskFailure;

@Repository
public class InMemoryCouponBatchTaskFailureRepository implements CouponBatchTaskFailureRepository {

    private final AtomicLong idGenerator = new AtomicLong(7000);
    private final ConcurrentMap<Long, CouponBatchTaskFailure> failures = new ConcurrentHashMap<>();

    @Override
    public CouponBatchTaskFailure save(CouponBatchTaskFailure failure) {
        if (failure.getId() == null) {
            failure.assignId(idGenerator.incrementAndGet());
        }
        failures.put(failure.getId(), failure);
        return failure;
    }

    @Override
    public List<CouponBatchTaskFailure> findByTaskId(Long taskId) {
        return failures.values()
                .stream()
                .filter(failure -> failure.getTaskId().equals(taskId))
                .sorted(Comparator.comparing(CouponBatchTaskFailure::getRowNumber))
                .toList();
    }
}
