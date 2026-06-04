package com.xinjia.coupon.settlement.infrastructure;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.xinjia.coupon.common.enums.CouponSettlementStatus;
import com.xinjia.coupon.settlement.domain.CouponSettlement;

public class InMemoryCouponSettlementRepository implements CouponSettlementRepository {

    private final AtomicLong idGenerator = new AtomicLong(11000);
    private final ConcurrentMap<Long, CouponSettlement> settlements = new ConcurrentHashMap<>();

    @Override
    public CouponSettlement save(CouponSettlement settlement) {
        if (settlement.getId() == null) {
            settlement.assignId(idGenerator.incrementAndGet());
        }
        settlements.put(settlement.getId(), settlement);
        return settlement;
    }

    @Override
    public Optional<CouponSettlement> findLocked(Long userId, Long userCouponId, String orderNo) {
        return findByStatus(userId, userCouponId, orderNo, CouponSettlementStatus.LOCKED);
    }

    @Override
    public Optional<CouponSettlement> findPaid(Long userId, Long userCouponId, String orderNo) {
        return findByStatus(userId, userCouponId, orderNo, CouponSettlementStatus.PAID);
    }

    @Override
    public boolean existsActive(Long userId, Long userCouponId) {
        return settlements.values()
                .stream()
                .anyMatch(settlement -> settlement.getUserId().equals(userId)
                        && settlement.getUserCouponId().equals(userCouponId)
                        && (settlement.getStatus() == CouponSettlementStatus.LOCKED
                        || settlement.getStatus() == CouponSettlementStatus.PAID));
    }

    private Optional<CouponSettlement> findByStatus(
            Long userId,
            Long userCouponId,
            String orderNo,
            CouponSettlementStatus status
    ) {
        return settlements.values()
                .stream()
                .filter(settlement -> settlement.getUserId().equals(userId))
                .filter(settlement -> settlement.getUserCouponId().equals(userCouponId))
                .filter(settlement -> settlement.getOrderNo().equals(orderNo))
                .filter(settlement -> settlement.getStatus() == status)
                .findFirst();
    }
}
