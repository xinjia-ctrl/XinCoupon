package com.xinjia.coupon.user.coupon.infrastructure;

import com.xinjia.coupon.user.coupon.domain.UserCoupon;

public interface UserCouponRepository {

    UserCoupon save(UserCoupon userCoupon);
}
