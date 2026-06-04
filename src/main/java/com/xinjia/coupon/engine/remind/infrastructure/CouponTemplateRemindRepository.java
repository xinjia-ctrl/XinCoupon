package com.xinjia.coupon.engine.remind.infrastructure;

import java.util.List;
import java.util.Optional;

import com.xinjia.coupon.common.enums.CouponTemplateRemindStatus;
import com.xinjia.coupon.engine.remind.domain.CouponTemplateRemind;

public interface CouponTemplateRemindRepository {

    CouponTemplateRemind save(CouponTemplateRemind remind);

    Optional<CouponTemplateRemind> findById(Long id);

    Optional<CouponTemplateRemind> findActiveByUserIdAndTemplateId(Long userId, Long templateId);

    List<CouponTemplateRemind> findByUserId(Long userId, CouponTemplateRemindStatus status);
}
