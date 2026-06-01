package com.xinjia.coupon.admin.campaign.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Repository;

import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.admin.campaign.infrastructure.CouponCampaignRepository;

@Repository
public class MySqlCouponCampaignRepository implements CouponCampaignRepository {

    private final CouponCampaignMapper couponCampaignMapper;
    private final CouponCampaignConverter couponCampaignConverter;

    public MySqlCouponCampaignRepository(
            CouponCampaignMapper couponCampaignMapper,
            CouponCampaignConverter couponCampaignConverter
    ) {
        this.couponCampaignMapper = couponCampaignMapper;
        this.couponCampaignConverter = couponCampaignConverter;
    }

    @Override
    public CouponCampaign save(CouponCampaign campaign) {
        CouponCampaignDO dataObject = couponCampaignConverter.toDO(campaign);
        if (dataObject.getId() == null) {
            couponCampaignMapper.insert(dataObject);
            return couponCampaignConverter.toDomain(dataObject);
        }

        couponCampaignMapper.updateById(dataObject);
        return findById(dataObject.getId()).orElseGet(() -> couponCampaignConverter.toDomain(dataObject));
    }

    @Override
    public Optional<CouponCampaign> findById(Long id) {
        return Optional.ofNullable(couponCampaignMapper.selectById(id))
                .map(couponCampaignConverter::toDomain);
    }

    @Override
    public List<CouponCampaign> findAll() {
        return couponCampaignMapper.selectList(
                        Wrappers.lambdaQuery(CouponCampaignDO.class)
                                .orderByDesc(CouponCampaignDO::getId)
                )
                .stream()
                .map(couponCampaignConverter::toDomain)
                .toList();
    }
}
