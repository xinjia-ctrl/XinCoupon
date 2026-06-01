package com.xinjia.coupon.admin.template.infrastructure;

import java.util.List;
import java.util.Optional;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.enums.CouponTemplateStatus;

public interface CouponTemplateRepository {

    CouponTemplate save(CouponTemplate template);

    Optional<CouponTemplate> findById(Long id);

    List<CouponTemplate> findAll();

    Optional<CouponTemplate> updateStatus(Long id, CouponTemplateStatus status);
}
