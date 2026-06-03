package com.xinjia.coupon.distribution.task.infrastructure;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.xinjia.coupon.distribution.task.domain.CouponBatchTask;

@Repository
public class InMemoryCouponBatchTaskRepository implements CouponBatchTaskRepository {

    private final AtomicLong idGenerator = new AtomicLong(5000);
    private final ConcurrentMap<Long, CouponBatchTask> tasks = new ConcurrentHashMap<>();

    @Override
    public CouponBatchTask save(CouponBatchTask task) {
        if (task.getId() == null) {
            task.assignId(idGenerator.incrementAndGet());
        }
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<CouponBatchTask> findById(Long taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    @Override
    public Optional<CouponBatchTask> findByBatchNo(String batchNo) {
        return tasks.values()
                .stream()
                .filter(task -> task.getBatchNo().equals(batchNo))
                .findFirst();
    }
}
