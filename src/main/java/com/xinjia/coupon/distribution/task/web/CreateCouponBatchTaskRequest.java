package com.xinjia.coupon.distribution.task.web;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateCouponBatchTaskRequest(
        @NotBlank @Size(max = 64) String batchNo,
        @NotNull @Positive Long campaignId,
        @NotEmpty @Size(max = 1000) List<@NotNull @Positive Long> userIds
) {
}
