package com.xinjia.coupon.distribution.task.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xinjia.coupon.common.api.ApiResponse;
import com.xinjia.coupon.distribution.task.application.CouponBatchTaskService;
import com.xinjia.coupon.distribution.task.domain.CouponBatchTask;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/coupon-batch-tasks")
public class CouponBatchTaskController {

    private final CouponBatchTaskService couponBatchTaskService;

    public CouponBatchTaskController(CouponBatchTaskService couponBatchTaskService) {
        this.couponBatchTaskService = couponBatchTaskService;
    }

    @PostMapping
    public ApiResponse<CouponBatchTaskView> create(@Valid @RequestBody CreateCouponBatchTaskRequest request) {
        CouponBatchTask task = couponBatchTaskService.create(request);
        couponBatchTaskService.dispatchAsync(task.getId(), request.userIds());
        return ApiResponse.success(CouponBatchTaskView.from(task));
    }

    @GetMapping("/{taskId}")
    public ApiResponse<CouponBatchTaskView> getById(@PathVariable Long taskId) {
        return ApiResponse.success(CouponBatchTaskView.from(couponBatchTaskService.getById(taskId)));
    }
}
