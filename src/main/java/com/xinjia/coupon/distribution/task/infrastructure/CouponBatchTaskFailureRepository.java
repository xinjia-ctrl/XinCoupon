package com.xinjia.coupon.distribution.task.infrastructure;

import java.util.List;

import com.xinjia.coupon.distribution.task.domain.CouponBatchTaskFailure;

public interface CouponBatchTaskFailureRepository {

    CouponBatchTaskFailure save(CouponBatchTaskFailure failure);

    List<CouponBatchTaskFailure> findByTaskId(Long taskId);
}
