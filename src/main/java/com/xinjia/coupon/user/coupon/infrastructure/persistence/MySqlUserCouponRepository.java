package com.xinjia.coupon.user.coupon.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Repository;

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
    public long countByUserIdAndCampaignId(Long userId, Long campaignId) {
        return userCouponMapper.selectCount(
                Wrappers.lambdaQuery(UserCouponDO.class)
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getCampaignId, campaignId)
        );
    }
}
