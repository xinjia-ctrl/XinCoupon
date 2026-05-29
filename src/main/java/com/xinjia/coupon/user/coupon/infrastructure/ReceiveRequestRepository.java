package com.xinjia.coupon.user.coupon.infrastructure;

import java.util.Optional;

import com.xinjia.coupon.user.coupon.domain.UserCoupon;

public interface ReceiveRequestRepository {

    Optional<UserCoupon> findResult(String requestId);

    void saveResult(String requestId, UserCoupon userCoupon);
}
