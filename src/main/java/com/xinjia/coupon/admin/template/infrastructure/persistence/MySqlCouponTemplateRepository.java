package com.xinjia.coupon.admin.template.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Repository;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.admin.template.infrastructure.CouponTemplateRepository;
import com.xinjia.coupon.common.enums.CouponTemplateStatus;

@Repository
public class MySqlCouponTemplateRepository implements CouponTemplateRepository {

    private final CouponTemplateMapper couponTemplateMapper;
    private final CouponTemplateConverter couponTemplateConverter;

    public MySqlCouponTemplateRepository(
            CouponTemplateMapper couponTemplateMapper,
            CouponTemplateConverter couponTemplateConverter
    ) {
        this.couponTemplateMapper = couponTemplateMapper;
        this.couponTemplateConverter = couponTemplateConverter;
    }

    @Override
    public CouponTemplate save(CouponTemplate template) {
        CouponTemplateDO dataObject = couponTemplateConverter.toDO(template);
        if (dataObject.getId() == null) {
            couponTemplateMapper.insert(dataObject);
            return couponTemplateConverter.toDomain(dataObject);
        }

        couponTemplateMapper.updateById(dataObject);
        return findById(dataObject.getId()).orElseGet(() -> couponTemplateConverter.toDomain(dataObject));
    }

    @Override
    public Optional<CouponTemplate> findById(Long id) {
        return Optional.ofNullable(couponTemplateMapper.selectById(id))
                .map(couponTemplateConverter::toDomain);
    }

    @Override
    public List<CouponTemplate> findAll() {
        return couponTemplateMapper.selectList(
                        Wrappers.lambdaQuery(CouponTemplateDO.class)
                                .orderByDesc(CouponTemplateDO::getId)
                )
                .stream()
                .map(couponTemplateConverter::toDomain)
                .toList();
    }

    @Override
    public Optional<CouponTemplate> updateStatus(Long id, CouponTemplateStatus status) {
        int updatedRows = couponTemplateMapper.update(
                null,
                Wrappers.lambdaUpdate(CouponTemplateDO.class)
                        .set(CouponTemplateDO::getStatus, status.name())
                        .set(CouponTemplateDO::getUpdatedAt, LocalDateTime.now())
                        .eq(CouponTemplateDO::getId, id)
        );
        if (updatedRows == 0) {
            return Optional.empty();
        }
        return findById(id);
    }

    @Override
    public Optional<CouponTemplate> increaseStock(Long id, Integer increasedStock) {
        int updatedRows = couponTemplateMapper.update(
                null,
                Wrappers.lambdaUpdate(CouponTemplateDO.class)
                        .setSql("total_stock = total_stock + " + increasedStock)
                        .set(CouponTemplateDO::getUpdatedAt, LocalDateTime.now())
                        .eq(CouponTemplateDO::getId, id)
        );
        if (updatedRows == 0) {
            return Optional.empty();
        }
        return findById(id);
    }
}
