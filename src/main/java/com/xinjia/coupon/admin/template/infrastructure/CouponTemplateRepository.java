package com.xinjia.coupon.admin.template.infrastructure;

import java.util.List;
import java.util.Optional;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;

public interface CouponTemplateRepository {

    CouponTemplate save(CouponTemplate template);

    Optional<CouponTemplate> findById(Long id);

    List<CouponTemplate> findAll();
}
