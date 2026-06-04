package com.xinjia.coupon.settlement.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinjia.coupon.common.enums.CouponSettlementStatus;
import com.xinjia.coupon.settlement.domain.CouponSettlement;
import com.xinjia.coupon.settlement.infrastructure.CouponSettlementRepository;

@Repository
public class MySqlCouponSettlementRepository implements CouponSettlementRepository {

    private final CouponSettlementMapper couponSettlementMapper;
    private final CouponSettlementConverter couponSettlementConverter;

    public MySqlCouponSettlementRepository(
            CouponSettlementMapper couponSettlementMapper,
            CouponSettlementConverter couponSettlementConverter
    ) {
        this.couponSettlementMapper = couponSettlementMapper;
        this.couponSettlementConverter = couponSettlementConverter;
    }

    @Override
    public CouponSettlement save(CouponSettlement settlement) {
        CouponSettlementDO dataObject = couponSettlementConverter.toDO(settlement);
        if (dataObject.getId() == null) {
            couponSettlementMapper.insert(dataObject);
            return couponSettlementConverter.toDomain(dataObject);
        }
        couponSettlementMapper.updateById(dataObject);
        return couponSettlementConverter.toDomain(couponSettlementMapper.selectById(dataObject.getId()));
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
        return couponSettlementMapper.selectCount(
                Wrappers.lambdaQuery(CouponSettlementDO.class)
                        .eq(CouponSettlementDO::getUserId, userId)
                        .eq(CouponSettlementDO::getUserCouponId, userCouponId)
                        .in(CouponSettlementDO::getStatus,
                                CouponSettlementStatus.LOCKED.name(),
                                CouponSettlementStatus.PAID.name())
        ) > 0;
    }

    private Optional<CouponSettlement> findByStatus(
            Long userId,
            Long userCouponId,
            String orderNo,
            CouponSettlementStatus status
    ) {
        return Optional.ofNullable(couponSettlementMapper.selectOne(
                        Wrappers.lambdaQuery(CouponSettlementDO.class)
                                .eq(CouponSettlementDO::getUserId, userId)
                                .eq(CouponSettlementDO::getUserCouponId, userCouponId)
                                .eq(CouponSettlementDO::getOrderNo, orderNo)
                                .eq(CouponSettlementDO::getStatus, status.name())
                                .last("limit 1")
                ))
                .map(couponSettlementConverter::toDomain);
    }
}
