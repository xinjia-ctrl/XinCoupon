package com.xinjia.coupon.distribution.task.application;

import java.util.List;

public interface CouponBatchUserStagingStore {

    BatchStageResult stage(Long taskId, Long campaignId, BatchUserRow row, Integer perUserLimit);

    List<BatchUserRow> pop(Long taskId, int count);

    long size(Long taskId);

    void restoreReservation(Long campaignId, Long userId);

    void clear(Long taskId);
}
