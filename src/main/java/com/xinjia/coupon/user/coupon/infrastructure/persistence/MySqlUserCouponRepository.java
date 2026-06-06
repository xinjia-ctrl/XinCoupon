package com.xinjia.coupon.user.coupon.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Repository;

import com.xinjia.coupon.common.sharding.CouponShardRouter;
import com.xinjia.coupon.common.sharding.ShardTarget;
import com.xinjia.coupon.common.sharding.ShardingProperties;
import com.xinjia.coupon.common.sharding.ShardingTableContext;
import com.xinjia.coupon.common.enums.UserCouponStatus;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;
import com.xinjia.coupon.user.coupon.infrastructure.UserCouponRepository;

@Repository
public class MySqlUserCouponRepository implements UserCouponRepository {

    private final UserCouponMapper userCouponMapper;
    private final UserCouponConverter userCouponConverter;
    private final CouponShardRouter couponShardRouter;
    private final ShardingProperties shardingProperties;

    public MySqlUserCouponRepository(
            UserCouponMapper userCouponMapper,
            UserCouponConverter userCouponConverter,
            CouponShardRouter couponShardRouter,
            ShardingProperties shardingProperties
    ) {
        this.userCouponMapper = userCouponMapper;
        this.userCouponConverter = userCouponConverter;
        this.couponShardRouter = couponShardRouter;
        this.shardingProperties = shardingProperties;
    }

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        return withUserCouponShard(userCoupon.getUserId(), () -> {
            UserCouponDO dataObject = userCouponConverter.toDO(userCoupon);
            if (dataObject.getId() == null) {
                userCouponMapper.insert(dataObject);
                return userCouponConverter.toDomain(dataObject);
            }

            userCouponMapper.updateById(dataObject);
            return Optional.ofNullable(userCouponMapper.selectById(dataObject.getId()))
                    .map(userCouponConverter::toDomain)
                    .orElseGet(() -> userCouponConverter.toDomain(dataObject));
        });
    }

    @Override
    public List<UserCoupon> saveBatch(List<UserCoupon> userCoupons) {
        if (userCoupons.isEmpty()) {
            return List.of();
        }
        if (!shardingProperties.isManualEnabled()) {
            return doSaveBatch(userCoupons);
        }
        List<UserCoupon> savedCoupons = new ArrayList<>(userCoupons.size());
        Map<ShardTarget, List<UserCoupon>> couponsByShard = userCoupons.stream()
                .collect(Collectors.groupingBy(coupon -> couponShardRouter.routeUserCoupon(coupon.getUserId())));
        couponsByShard.forEach((shardTarget, shardCoupons) -> savedCoupons.addAll(ShardingTableContext.use(
                shardTarget.logicalTable(),
                shardTarget.actualTable(),
                () -> doSaveBatch(shardCoupons)
        )));
        return savedCoupons;
    }

    private List<UserCoupon> doSaveBatch(List<UserCoupon> userCoupons) {
        List<UserCouponDO> dataObjects = userCoupons.stream()
                .map(userCouponConverter::toDO)
                .toList();
        userCouponMapper.insert(dataObjects, dataObjects.size());
        return dataObjects.stream()
                .map(userCouponConverter::toDomain)
                .toList();
    }

    @Override
    public Optional<UserCoupon> findById(Long id) {
        if (!shardingProperties.isManualEnabled()) {
            return Optional.ofNullable(userCouponMapper.selectById(id))
                    .map(userCouponConverter::toDomain);
        }
        return IntStream.range(0, shardingProperties.getUserCouponTableCount())
                .mapToObj(index -> ShardingTableContext.use("user_coupon", "user_coupon_" + index,
                        () -> Optional.ofNullable(userCouponMapper.selectById(id))
                                .map(userCouponConverter::toDomain)))
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public List<UserCoupon> findByUserId(Long userId) {
        return withUserCouponShard(userId, () -> userCouponMapper.selectList(
                        Wrappers.lambdaQuery(UserCouponDO.class)
                                .eq(UserCouponDO::getUserId, userId)
                                .orderByDesc(UserCouponDO::getReceivedAt)
                )
                .stream()
                .map(userCouponConverter::toDomain)
                .toList());
    }

    @Override
    public List<UserCoupon> findByUserIdAndStatus(Long userId, UserCouponStatus status) {
        return withUserCouponShard(userId, () -> userCouponMapper.selectList(
                        Wrappers.lambdaQuery(UserCouponDO.class)
                                .eq(UserCouponDO::getUserId, userId)
                                .eq(UserCouponDO::getStatus, status.name())
                                .orderByDesc(UserCouponDO::getReceivedAt)
                )
                .stream()
                .map(userCouponConverter::toDomain)
                .toList());
    }

    @Override
    public long countByUserIdAndCampaignId(Long userId, Long campaignId) {
        return withUserCouponShard(userId, () -> userCouponMapper.selectCount(
                Wrappers.lambdaQuery(UserCouponDO.class)
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getCampaignId, campaignId)
        ));
    }

    @Override
    public Optional<UserCoupon> lock(Long id, String orderNo) {
        Long userId = findById(id).map(UserCoupon::getUserId).orElse(null);
        if (userId == null) {
            return Optional.empty();
        }
        return withUserCouponShard(userId, () -> doLock(id, orderNo));
    }

    private Optional<UserCoupon> doLock(Long id, String orderNo) {
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
        Long userId = findById(id).map(UserCoupon::getUserId).orElse(null);
        if (userId == null) {
            return Optional.empty();
        }
        return withUserCouponShard(userId, () -> doConfirmUse(id));
    }

    private Optional<UserCoupon> doConfirmUse(Long id) {
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
        Long userId = findById(id).map(UserCoupon::getUserId).orElse(null);
        if (userId == null) {
            return Optional.empty();
        }
        return withUserCouponShard(userId, () -> doRelease(id));
    }

    private Optional<UserCoupon> doRelease(Long id) {
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
        Long userId = findById(id).map(UserCoupon::getUserId).orElse(null);
        if (userId == null) {
            return Optional.empty();
        }
        return withUserCouponShard(userId, () -> doRefund(id));
    }

    private Optional<UserCoupon> doRefund(Long id) {
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

    private <T> T withUserCouponShard(Long userId, java.util.function.Supplier<T> supplier) {
        if (!shardingProperties.isManualEnabled()) {
            return supplier.get();
        }
        ShardTarget shardTarget = couponShardRouter.routeUserCoupon(userId);
        return ShardingTableContext.use(shardTarget.logicalTable(), shardTarget.actualTable(), supplier);
    }
}
