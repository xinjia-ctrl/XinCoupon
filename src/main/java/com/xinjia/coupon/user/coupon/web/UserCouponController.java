package com.xinjia.coupon.user.coupon.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xinjia.coupon.common.api.ApiResponse;
import com.xinjia.coupon.common.auth.RequestIdentityResolver;
import com.xinjia.coupon.common.enums.UserCouponStatus;
import com.xinjia.coupon.user.coupon.application.UserCouponService;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user/coupons")
public class UserCouponController {

    private final UserCouponService userCouponService;

    public UserCouponController(UserCouponService userCouponService) {
        this.userCouponService = userCouponService;
    }

    @PostMapping("/receive")
    public ApiResponse<UserCouponView> receive(@Valid @RequestBody ReceiveCouponRequest request) {
        Long userId = RequestIdentityResolver.resolveUserId(request.userId());
        UserCoupon userCoupon = userCouponService.receive(request.withUserId(userId));
        return ApiResponse.success(UserCouponView.from(userCoupon));
    }

    @GetMapping
    public ApiResponse<List<UserCouponView>> listByUserId(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) UserCouponStatus status
    ) {
        Long resolvedUserId = RequestIdentityResolver.resolveUserId(userId);
        List<UserCoupon> queriedCoupons = status == null
                ? userCouponService.listByUserId(resolvedUserId)
                : userCouponService.listByUserIdAndStatus(resolvedUserId, status);
        List<UserCouponView> userCoupons = queriedCoupons
                .stream()
                .map(UserCouponView::from)
                .toList();
        return ApiResponse.success(userCoupons);
    }
}
