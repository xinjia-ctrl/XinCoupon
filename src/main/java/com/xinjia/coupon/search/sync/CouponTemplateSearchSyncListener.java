package com.xinjia.coupon.search.sync;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.search.infrastructure.CouponTemplateSearchIndex;

@Component
public class CouponTemplateSearchSyncListener {

    private final CouponTemplateSearchIndex couponTemplateSearchIndex;

    public CouponTemplateSearchSyncListener(CouponTemplateSearchIndex couponTemplateSearchIndex) {
        this.couponTemplateSearchIndex = couponTemplateSearchIndex;
    }

    @EventListener
    public void handle(CouponTemplateSearchSyncEvent event) {
        couponTemplateSearchIndex.save(event.document());
    }
}
