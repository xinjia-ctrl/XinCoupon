package com.xinjia.coupon.distribution.task.infrastructure;

import java.util.Optional;

import com.xinjia.coupon.distribution.task.domain.CouponBatchTask;

public interface CouponBatchTaskRepository {

    CouponBatchTask save(CouponBatchTask task);

    Optional<CouponBatchTask> findById(Long taskId);

    Optional<CouponBatchTask> findByBatchNo(String batchNo);
}
