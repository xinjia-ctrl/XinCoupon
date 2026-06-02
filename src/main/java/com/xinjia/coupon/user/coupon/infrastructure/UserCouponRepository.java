package com.xinjia.coupon.user.coupon.infrastructure;

import java.util.List;
import java.util.Optional;

import com.xinjia.coupon.common.enums.UserCouponStatus;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;

public interface UserCouponRepository {

    UserCoupon save(UserCoupon userCoupon);

    Optional<UserCoupon> findById(Long id);

    List<UserCoupon> findByUserId(Long userId);

    List<UserCoupon> findByUserIdAndStatus(Long userId, UserCouponStatus status);

    long countByUserIdAndCampaignId(Long userId, Long campaignId);

    Optional<UserCoupon> lock(Long id, String orderNo);

    Optional<UserCoupon> confirmUse(Long id);

    Optional<UserCoupon> release(Long id);
}
