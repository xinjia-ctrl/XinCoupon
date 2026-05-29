package com.xinjia.coupon.settlement.web;

import java.util.List;

public record SettlementCalculateView(
        Long userId,
        String orderNo,
        Long merchantId,
        Long orderAmount,
        Long bestDiscountAmount,
        Long payableAmount,
        List<AvailableCouponView> availableCoupons,
        AvailableCouponView bestCoupon
) {
}
