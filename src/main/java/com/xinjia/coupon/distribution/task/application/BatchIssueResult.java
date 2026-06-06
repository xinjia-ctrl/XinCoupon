package com.xinjia.coupon.distribution.task.application;

import java.util.List;

public record BatchIssueResult(int successCount, List<BatchIssueFailure> failures) {

    public static BatchIssueResult success(int successCount) {
        return new BatchIssueResult(successCount, List.of());
    }
}
