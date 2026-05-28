package com.xinjia.coupon.admin.template.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.api.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/coupon-templates")
public class CouponTemplateController {

    private final CouponTemplateService couponTemplateService;

    public CouponTemplateController(CouponTemplateService couponTemplateService) {
        this.couponTemplateService = couponTemplateService;
    }

    @PostMapping
    public ApiResponse<CouponTemplateView> create(@Valid @RequestBody CreateCouponTemplateRequest request) {
        CouponTemplate template = couponTemplateService.create(request);
        return ApiResponse.success(CouponTemplateView.from(template));
    }
}
