package com.xinjia.coupon.user.coupon.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Repository;

import com.xinjia.coupon.common.enums.UserCouponStatus;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;
import com.xinjia.coupon.user.coupon.infrastructure.UserCouponRepository;

@Repository
public class MySqlUserCouponRepository implements UserCouponRepository {

    private final UserCouponMapper userCouponMapper;
    private final UserCouponConverter userCouponConverter;

    public MySqlUserCouponRepository(
            UserCouponMapper userCouponMapper,
            UserCouponConverter userCouponConverter
    ) {
        this.userCouponMapper = userCouponMapper;
        this.userCouponConverter = userCouponConverter;
    }

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        UserCouponDO dataObject = userCouponConverter.toDO(userCoupon);
        if (dataObject.getId() == null) {
            userCouponMapper.insert(dataObject);
            return userCouponConverter.toDomain(dataObject);
        }

        userCouponMapper.updateById(dataObject);
        return findById(dataObject.getId()).orElseGet(() -> userCouponConverter.toDomain(dataObject));
    }

    @Override
    public Optional<UserCoupon> findById(Long id) {
        return Optional.ofNullable(userCouponMapper.selectById(id))
                .map(userCouponConverter::toDomain);
    }

    @Override
    public List<UserCoupon> findByUserId(Long userId) {
        return userCouponMapper.selectList(
                        Wrappers.lambdaQuery(UserCouponDO.class)
                                .eq(UserCouponDO::getUserId, userId)
                                .orderByDesc(UserCouponDO::getReceivedAt)
                )
                .stream()
                .map(userCouponConverter::toDomain)
                .toList();
    }

    @Override
    public List<UserCoupon> findByUserIdAndStatus(Long userId, UserCouponStatus status) {
        return userCouponMapper.selectList(
                        Wrappers.lambdaQuery(UserCouponDO.class)
                                .eq(UserCouponDO::getUserId, userId)
                                .eq(UserCouponDO::getStatus, status.name())
                                .orderByDesc(UserCouponDO::getReceivedAt)
                )
                .stream()
                .map(userCouponConverter::toDomain)
                .toList();
    }

    @Override
    public long countByUserIdAndCampaignId(Long userId, Long campaignId) {
        return userCouponMapper.selectCount(
                Wrappers.lambdaQuery(UserCouponDO.class)
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getCampaignId, campaignId)
        );
    }

    @Override
    public Optional<UserCoupon> lock(Long id, String orderNo) {
        LocalDateTime now = LocalDateTime.now();
        int updatedRows = userCouponMapper.update(
                null,
                Wrappers.lambdaUpdate(UserCouponDO.class)
                        .set(UserCouponDO::getStatus, UserCouponStatus.LOCKED.name())
                        .set(UserCouponDO::getLockedAt, now)
                        .set(UserCouponDO::getOrderNo, orderNo)
                        .set(UserCouponDO::getUpdatedAt, now)
                        .eq(UserCouponDO::getId, id)
                        .eq(UserCouponDO::getStatus, UserCouponStatus.RECEIVED.name())
        );
        if (updatedRows == 0) {
            return Optional.empty();
        }
        return findById(id);
    }

    @Override
    public Optional<UserCoupon> confirmUse(Long id) {
        LocalDateTime now = LocalDateTime.now();
        int updatedRows = userCouponMapper.update(
                null,
                Wrappers.lambdaUpdate(UserCouponDO.class)
                        .set(UserCouponDO::getStatus, UserCouponStatus.USED.name())
                        .set(UserCouponDO::getUsedAt, now)
                        .set(UserCouponDO::getUpdatedAt, now)
                        .eq(UserCouponDO::getId, id)
                        .eq(UserCouponDO::getStatus, UserCouponStatus.LOCKED.name())
        );
        if (updatedRows == 0) {
            return Optional.empty();
        }
        return findById(id);
    }

    @Override
    public Optional<UserCoupon> release(Long id) {
        LocalDateTime now = LocalDateTime.now();
        int updatedRows = userCouponMapper.update(
                null,
                Wrappers.lambdaUpdate(UserCouponDO.class)
                        .set(UserCouponDO::getStatus, UserCouponStatus.RECEIVED.name())
                        .set(UserCouponDO::getLockedAt, null)
                        .set(UserCouponDO::getOrderNo, null)
                        .set(UserCouponDO::getUpdatedAt, now)
                        .eq(UserCouponDO::getId, id)
                        .eq(UserCouponDO::getStatus, UserCouponStatus.LOCKED.name())
        );
        if (updatedRows == 0) {
            return Optional.empty();
        }
        return findById(id);
    }

    @Override
    public Optional<UserCoupon> refund(Long id) {
        LocalDateTime now = LocalDateTime.now();
        int updatedRows = userCouponMapper.update(
                null,
                Wrappers.lambdaUpdate(UserCouponDO.class)
                        .set(UserCouponDO::getStatus, UserCouponStatus.RECEIVED.name())
                        .set(UserCouponDO::getLockedAt, null)
                        .set(UserCouponDO::getUsedAt, null)
                        .set(UserCouponDO::getOrderNo, null)
                        .set(UserCouponDO::getUpdatedAt, now)
                        .eq(UserCouponDO::getId, id)
                        .eq(UserCouponDO::getStatus, UserCouponStatus.USED.name())
        );
        if (updatedRows == 0) {
            return Optional.empty();
        }
        return findById(id);
    }
}
