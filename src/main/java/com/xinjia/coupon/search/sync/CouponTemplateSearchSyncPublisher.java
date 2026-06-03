package com.xinjia.coupon.search.sync;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.admin.template.application.CouponTemplateChangePublisher;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.search.domain.CouponTemplateSearchDocument;

@Component
public class CouponTemplateSearchSyncPublisher implements CouponTemplateChangePublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public CouponTemplateSearchSyncPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(CouponTemplate template) {
        applicationEventPublisher.publishEvent(new CouponTemplateSearchSyncEvent(
                CouponTemplateSearchDocument.from(template)
        ));
    }
}
