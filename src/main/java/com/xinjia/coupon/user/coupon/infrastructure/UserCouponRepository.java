package com.xinjia.coupon.user.coupon.infrastructure;

import java.util.List;
import java.util.Optional;

import com.xinjia.coupon.user.coupon.domain.UserCoupon;

public interface UserCouponRepository {

    UserCoupon save(UserCoupon userCoupon);

    Optional<UserCoupon> findById(Long id);

    List<UserCoupon> findByUserId(Long userId);

    long countByUserIdAndCampaignId(Long userId, Long campaignId);
}
