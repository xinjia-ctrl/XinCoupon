package com.xinjia.coupon.distribution.task.web;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xinjia.coupon.common.api.ApiResponse;
import com.xinjia.coupon.common.api.PageResult;
import com.xinjia.coupon.common.enums.CouponBatchTaskStatus;
import com.xinjia.coupon.common.idempotent.NoDuplicateSubmit;
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
    @NoDuplicateSubmit(key = "'coupon-batch-task:create:' + #request.batchNo()", ttlSeconds = 10)
    public ApiResponse<CouponBatchTaskView> create(@Valid @RequestBody CreateCouponBatchTaskRequest request) {
        CouponBatchTask task = couponBatchTaskService.create(request);
        couponBatchTaskService.dispatchAsync(task.getId(), request.userIds());
        return ApiResponse.success(CouponBatchTaskView.from(task));
    }

    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @NoDuplicateSubmit(key = "'coupon-batch-task:excel:' + #batchNo", ttlSeconds = 10)
    public ApiResponse<CouponBatchTaskView> createFromExcel(
            @RequestParam String batchNo,
            @RequestParam Long campaignId,
            @RequestParam MultipartFile file
    ) throws java.io.IOException {
        CouponBatchTask task = couponBatchTaskService.createFromExcel(batchNo, campaignId, file.getInputStream());
        return ApiResponse.success(CouponBatchTaskView.from(task));
    }

    @GetMapping("/{taskId}")
    public ApiResponse<CouponBatchTaskView> getById(@PathVariable Long taskId) {
        return ApiResponse.success(CouponBatchTaskView.from(couponBatchTaskService.getById(taskId)));
    }

    @GetMapping
    public ApiResponse<PageResult<CouponBatchTaskView>> page(
            @RequestParam(required = false) CouponBatchTaskStatus status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        PageResult<CouponBatchTask> result = couponBatchTaskService.page(status, pageNo, pageSize);
        return ApiResponse.success(new PageResult<>(
                result.records().stream().map(CouponBatchTaskView::from).toList(),
                result.total(),
                result.pageNo(),
                result.pageSize()
        ));
    }

    @GetMapping("/{taskId}/failures")
    public ApiResponse<List<CouponBatchTaskFailureView>> listFailures(@PathVariable Long taskId) {
        List<CouponBatchTaskFailureView> failures = couponBatchTaskService.listFailures(taskId)
                .stream()
                .map(CouponBatchTaskFailureView::from)
                .toList();
        return ApiResponse.success(failures);
    }

    @GetMapping("/{taskId}/failures/export")
    public ResponseEntity<byte[]> exportFailures(@PathVariable Long taskId) {
        byte[] content = couponBatchTaskService.exportFailures(taskId);
        String fileName = "coupon-batch-task-" + taskId + "-failures.xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(fileName)
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }
}
