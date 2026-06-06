package com.xinjia.coupon.distribution.task.application;

import java.util.ArrayList;
import java.util.List;

import com.xinjia.coupon.distribution.task.domain.CouponBatchTask;
import com.xinjia.coupon.user.coupon.application.UserCouponService;
import com.xinjia.coupon.user.coupon.web.ReceiveCouponRequest;

class LegacyCouponBatchIssueProcessor implements CouponBatchIssueProcessor {

    private final UserCouponService userCouponService;

    LegacyCouponBatchIssueProcessor(UserCouponService userCouponService) {
        this.userCouponService = userCouponService;
    }

    @Override
    public BatchIssueResult issue(CouponBatchTask task, List<BatchUserRow> rows) {
        int successCount = 0;
        List<BatchIssueFailure> failures = new ArrayList<>();
        for (BatchUserRow row : rows) {
            try {
                userCouponService.receive(new ReceiveCouponRequest(
                        task.getBatchNo() + "-" + row.userId(),
                        row.userId(),
                        task.getCampaignId()
                ));
                successCount++;
            } catch (RuntimeException exception) {
                failures.add(new BatchIssueFailure(row.userId(), row.rowNumber(), exception.getMessage()));
            }
        }
        return new BatchIssueResult(successCount, failures);
    }
}
