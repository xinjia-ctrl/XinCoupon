package com.xinjia.coupon.settlement.infrastructure;

import java.util.Optional;

import com.xinjia.coupon.settlement.domain.CouponSettlement;

public interface CouponSettlementRepository {

    CouponSettlement save(CouponSettlement settlement);

    Optional<CouponSettlement> findLocked(Long userId, Long userCouponId, String orderNo);

    Optional<CouponSettlement> findPaid(Long userId, Long userCouponId, String orderNo);

    boolean existsActive(Long userId, Long userCouponId);
}
