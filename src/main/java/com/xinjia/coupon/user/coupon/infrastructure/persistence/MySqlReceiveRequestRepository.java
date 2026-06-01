package com.xinjia.coupon.user.coupon.infrastructure.persistence;

import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import com.xinjia.coupon.user.coupon.domain.UserCoupon;
import com.xinjia.coupon.user.coupon.infrastructure.ReceiveRequestRepository;

@Repository
public class MySqlReceiveRequestRepository implements ReceiveRequestRepository {

    private static final String SUCCESS = "SUCCESS";

    private final CouponReceiveRecordMapper couponReceiveRecordMapper;
    private final UserCouponMapper userCouponMapper;
    private final UserCouponConverter userCouponConverter;

    public MySqlReceiveRequestRepository(
            CouponReceiveRecordMapper couponReceiveRecordMapper,
            UserCouponMapper userCouponMapper,
            UserCouponConverter userCouponConverter
    ) {
        this.couponReceiveRecordMapper = couponReceiveRecordMapper;
        this.userCouponMapper = userCouponMapper;
        this.userCouponConverter = userCouponConverter;
    }

    @Override
    public Optional<UserCoupon> findResult(String requestId) {
        CouponReceiveRecordDO record = couponReceiveRecordMapper.selectOne(
                Wrappers.lambdaQuery(CouponReceiveRecordDO.class)
                        .eq(CouponReceiveRecordDO::getRequestId, requestId)
                        .eq(CouponReceiveRecordDO::getResult, SUCCESS)
                        .last("limit 1")
        );
        if (record == null || record.getUserCouponId() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userCouponMapper.selectById(record.getUserCouponId()))
                .map(userCouponConverter::toDomain);
    }

    @Override
    public void saveResult(String requestId, UserCoupon userCoupon) {
        CouponReceiveRecordDO record = new CouponReceiveRecordDO();
        record.setRequestId(requestId);
        record.setUserId(userCoupon.getUserId());
        record.setCampaignId(userCoupon.getCampaignId());
        record.setTemplateId(userCoupon.getTemplateId());
        record.setUserCouponId(userCoupon.getId());
        record.setResult(SUCCESS);
        try {
            couponReceiveRecordMapper.insert(record);
        } catch (DuplicateKeyException ignored) {
            // 并发重复请求已由 request_id 唯一索引兜底，调用方下次会读到首个成功结果。
        }
    }
}
