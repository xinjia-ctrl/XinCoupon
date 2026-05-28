package com.xinjia.coupon.user.coupon.infrastructure;

import java.util.Comparator;
import java.util.List;
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

    @Override
    public List<UserCoupon> findByUserId(Long userId) {
        return userCoupons.values()
                .stream()
                .filter(userCoupon -> userCoupon.getUserId().equals(userId))
                .sorted(Comparator.comparing(UserCoupon::getReceivedAt).reversed())
                .toList();
    }

    @Override
    public long countByUserIdAndCampaignId(Long userId, Long campaignId) {
        return userCoupons.values()
                .stream()
                .filter(userCoupon -> userCoupon.getUserId().equals(userId))
                .filter(userCoupon -> userCoupon.getCampaignId().equals(campaignId))
                .count();
    }
}
