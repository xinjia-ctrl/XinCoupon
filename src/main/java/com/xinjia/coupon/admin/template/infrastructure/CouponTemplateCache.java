package com.xinjia.coupon.admin.template.infrastructure;

import java.time.Duration;
import java.util.Optional;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;

public interface CouponTemplateCache {

    Optional<CouponTemplate> get(Long templateId);

    boolean isNullValue(Long templateId);

    void put(CouponTemplate template, Duration ttl);

    void putNull(Long templateId, Duration ttl);

    static CouponTemplateCache noop() {
        return new CouponTemplateCache() {
            @Override
            public Optional<CouponTemplate> get(Long templateId) {
                return Optional.empty();
            }

            @Override
            public boolean isNullValue(Long templateId) {
                return false;
            }

            @Override
            public void put(CouponTemplate template, Duration ttl) {
            }

            @Override
            public void putNull(Long templateId, Duration ttl) {
            }
        };
    }
}
