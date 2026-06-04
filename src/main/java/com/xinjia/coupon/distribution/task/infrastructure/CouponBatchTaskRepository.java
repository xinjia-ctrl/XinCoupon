package com.xinjia.coupon.distribution.task.infrastructure;

import java.util.Optional;
import java.util.List;

import com.xinjia.coupon.common.enums.CouponBatchTaskStatus;
import com.xinjia.coupon.distribution.task.domain.CouponBatchTask;

public interface CouponBatchTaskRepository {

    CouponBatchTask save(CouponBatchTask task);

    Optional<CouponBatchTask> findById(Long taskId);

    Optional<CouponBatchTask> findByBatchNo(String batchNo);

    List<CouponBatchTask> findPage(CouponBatchTaskStatus status, int pageNo, int pageSize);

    long count(CouponBatchTaskStatus status);
}
