package com.xinjia.coupon.admin.template.application;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;

public interface CouponTemplateChangePublisher {

    void publish(CouponTemplate template);

    static CouponTemplateChangePublisher noop() {
        return template -> {
        };
    }
}
