package com.xinjia.coupon.distribution.task.application;

import java.util.List;

import com.xinjia.coupon.distribution.task.domain.CouponBatchTask;

public interface CouponBatchIssueProcessor {

    BatchIssueResult issue(CouponBatchTask task, List<BatchUserRow> rows);
}
