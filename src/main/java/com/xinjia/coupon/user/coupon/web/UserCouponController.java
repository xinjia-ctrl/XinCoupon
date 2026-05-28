package com.xinjia.coupon.user.coupon.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xinjia.coupon.common.api.ApiResponse;
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
        UserCoupon userCoupon = userCouponService.receive(request);
        return ApiResponse.success(UserCouponView.from(userCoupon));
    }
}
