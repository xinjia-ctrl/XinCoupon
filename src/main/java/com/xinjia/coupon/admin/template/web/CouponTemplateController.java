package com.xinjia.coupon.admin.template.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.api.ApiResponse;
import com.xinjia.coupon.common.idempotent.NoDuplicateSubmit;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/coupon-templates")
public class CouponTemplateController {

    private final CouponTemplateService couponTemplateService;

    public CouponTemplateController(CouponTemplateService couponTemplateService) {
        this.couponTemplateService = couponTemplateService;
    }

    @PostMapping
    @NoDuplicateSubmit(key = "'coupon-template:create:' + #request.merchantId() + ':' + #request.title()", ttlSeconds = 5)
    public ApiResponse<CouponTemplateView> create(@Valid @RequestBody CreateCouponTemplateRequest request) {
        CouponTemplate template = couponTemplateService.create(request);
        return ApiResponse.success(CouponTemplateView.from(template));
    }

    @GetMapping("/{templateId}")
    public ApiResponse<CouponTemplateView> getById(@PathVariable Long templateId) {
        CouponTemplate template = couponTemplateService.getById(templateId);
        return ApiResponse.success(CouponTemplateView.from(template));
    }

    @GetMapping
    public ApiResponse<List<CouponTemplateView>> list() {
        List<CouponTemplateView> templates = couponTemplateService.list()
                .stream()
                .map(CouponTemplateView::from)
                .toList();
        return ApiResponse.success(templates);
    }

    @PatchMapping("/{templateId}/status")
    public ApiResponse<CouponTemplateView> changeStatus(
            @PathVariable Long templateId,
            @Valid @RequestBody UpdateCouponTemplateStatusRequest request
    ) {
        CouponTemplate template = couponTemplateService.changeStatus(templateId, request);
        return ApiResponse.success(CouponTemplateView.from(template));
    }

    @PostMapping("/{templateId}/stock/increase")
    @NoDuplicateSubmit(key = "'coupon-template:increase-stock:' + #templateId + ':' + #request.increasedStock()", ttlSeconds = 5)
    public ApiResponse<CouponTemplateView> increaseStock(
            @PathVariable Long templateId,
            @Valid @RequestBody IncreaseCouponTemplateStockRequest request
    ) {
        CouponTemplate template = couponTemplateService.increaseStock(templateId, request);
        return ApiResponse.success(CouponTemplateView.from(template));
    }

    @PostMapping("/{templateId}/terminate")
    public ApiResponse<CouponTemplateView> terminate(@PathVariable Long templateId) {
        CouponTemplate template = couponTemplateService.terminate(templateId);
        return ApiResponse.success(CouponTemplateView.from(template));
    }
}
