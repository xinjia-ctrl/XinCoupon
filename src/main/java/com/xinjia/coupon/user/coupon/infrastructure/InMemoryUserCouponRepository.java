package com.xinjia.coupon.user.coupon.infrastructure;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.xinjia.coupon.user.coupon.domain.UserCoupon;

@Repository
public class InMemoryUserCouponRepository implements UserCouponRepository {

    private final AtomicLong idGenerator = new AtomicLong(3000);
    private final ConcurrentMap<Long, UserCoupon> userCoupons = new ConcurrentHashMap<>();

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        if (userCoupon.getId() == null) {
            userCoupon.assignId(idGenerator.incrementAndGet());
        }
        userCoupons.put(userCoupon.getId(), userCoupon);
        return userCoupon;
    }
}
