package com.xinjia.coupon.engine.remind.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xinjia.coupon.common.api.ApiResponse;
import com.xinjia.coupon.common.auth.RequestIdentityResolver;
import com.xinjia.coupon.common.enums.CouponTemplateRemindStatus;
import com.xinjia.coupon.engine.remind.application.CouponTemplateRemindService;
import com.xinjia.coupon.engine.remind.domain.CouponTemplateRemind;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/engine/coupon-template-reminds")
public class CouponTemplateRemindController {

    private final CouponTemplateRemindService couponTemplateRemindService;

    public CouponTemplateRemindController(CouponTemplateRemindService couponTemplateRemindService) {
        this.couponTemplateRemindService = couponTemplateRemindService;
    }

    @PostMapping
    public ApiResponse<CouponTemplateRemindView> create(
            @Valid @RequestBody CreateCouponTemplateRemindRequest request
    ) {
        Long userId = RequestIdentityResolver.resolveUserId(request.userId());
        CouponTemplateRemind remind = couponTemplateRemindService.create(request.withUserId(userId));
        return ApiResponse.success(CouponTemplateRemindView.from(remind));
    }

    @GetMapping
    public ApiResponse<List<CouponTemplateRemindView>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) CouponTemplateRemindStatus status
    ) {
        Long resolvedUserId = RequestIdentityResolver.resolveUserId(userId);
        List<CouponTemplateRemindView> reminds = couponTemplateRemindService.list(resolvedUserId, status)
                .stream()
                .map(CouponTemplateRemindView::from)
                .toList();
        return ApiResponse.success(reminds);
    }

    @PostMapping("/cancel")
    public ApiResponse<CouponTemplateRemindView> cancel(
            @Valid @RequestBody CancelCouponTemplateRemindRequest request
    ) {
        Long userId = RequestIdentityResolver.resolveUserId(request.userId());
        CouponTemplateRemind remind = couponTemplateRemindService.cancel(request.withUserId(userId));
        return ApiResponse.success(CouponTemplateRemindView.from(remind));
    }
}
