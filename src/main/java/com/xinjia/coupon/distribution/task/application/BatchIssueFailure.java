package com.xinjia.coupon.distribution.task.application;

public record BatchIssueFailure(Long userId, int rowNumber, String reason) {
}
