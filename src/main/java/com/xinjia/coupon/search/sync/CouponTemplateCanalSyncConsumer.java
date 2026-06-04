package com.xinjia.coupon.search.sync;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.search.infrastructure.CouponTemplateSearchIndex;

@Component
public class CouponTemplateCanalSyncConsumer {

    private final CouponTemplateSearchIndex couponTemplateSearchIndex;

    public CouponTemplateCanalSyncConsumer(CouponTemplateSearchIndex couponTemplateSearchIndex) {
        this.couponTemplateSearchIndex = couponTemplateSearchIndex;
    }

    @EventListener
    public void handle(CouponTemplateBinlogEvent event) {
        if ("DELETE".equalsIgnoreCase(event.operation())) {
            return;
        }
        couponTemplateSearchIndex.save(event.document());
    }
}
