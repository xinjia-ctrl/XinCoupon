package com.xinjia.coupon.settlement.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xinjia.coupon.common.api.ApiResponse;
import com.xinjia.coupon.common.auth.RequestIdentityResolver;
import com.xinjia.coupon.settlement.application.SettlementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/settlement")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping("/calculate")
    public ApiResponse<SettlementCalculateView> calculate(@Valid @RequestBody SettlementCalculateRequest request) {
        Long userId = RequestIdentityResolver.resolveUserId(request.userId());
        return ApiResponse.success(settlementService.calculate(request.withUserId(userId)));
    }

    @PostMapping("/lock")
    public ApiResponse<CouponOperationView> lock(@Valid @RequestBody CouponLockRequest request) {
        Long userId = RequestIdentityResolver.resolveUserId(request.userId());
        return ApiResponse.success(settlementService.lock(request.withUserId(userId)));
    }

    @PostMapping("/confirm")
    public ApiResponse<CouponOperationView> confirm(@Valid @RequestBody CouponConfirmRequest request) {
        Long userId = RequestIdentityResolver.resolveUserId(request.userId());
        return ApiResponse.success(settlementService.confirm(request.withUserId(userId)));
    }

    @PostMapping("/cancel")
    public ApiResponse<CouponOperationView> cancel(@Valid @RequestBody CouponCancelRequest request) {
        Long userId = RequestIdentityResolver.resolveUserId(request.userId());
        return ApiResponse.success(settlementService.cancel(request.withUserId(userId)));
    }

    @PostMapping("/refund")
    public ApiResponse<CouponOperationView> refund(@Valid @RequestBody CouponRefundRequest request) {
        Long userId = RequestIdentityResolver.resolveUserId(request.userId());
        return ApiResponse.success(settlementService.refund(request.withUserId(userId)));
    }
}
