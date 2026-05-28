package com.xinjia.coupon.user.coupon.infrastructure;

import java.util.List;

import com.xinjia.coupon.user.coupon.domain.UserCoupon;

public interface UserCouponRepository {

    UserCoupon save(UserCoupon userCoupon);

    List<UserCoupon> findByUserId(Long userId);

    long countByUserIdAndCampaignId(Long userId, Long campaignId);
}
