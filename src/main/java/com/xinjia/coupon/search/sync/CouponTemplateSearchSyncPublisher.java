package com.xinjia.coupon.search.sync;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.admin.template.application.CouponTemplateChangePublisher;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.search.domain.CouponTemplateSearchDocument;
import com.xinjia.coupon.search.sync.SearchSyncProperties.SyncMode;

@Component
public class CouponTemplateSearchSyncPublisher implements CouponTemplateChangePublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final SearchSyncProperties searchSyncProperties;
    private final CouponTemplateSearchSyncLogRepository couponTemplateSearchSyncLogRepository;

    public CouponTemplateSearchSyncPublisher(
            ApplicationEventPublisher applicationEventPublisher,
            SearchSyncProperties searchSyncProperties,
            CouponTemplateSearchSyncLogRepository couponTemplateSearchSyncLogRepository
    ) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.searchSyncProperties = searchSyncProperties;
        this.couponTemplateSearchSyncLogRepository = couponTemplateSearchSyncLogRepository;
    }

    @Override
    public void publish(CouponTemplate template) {
        CouponTemplateSearchDocument document = CouponTemplateSearchDocument.from(template);
        if (searchSyncProperties.getSyncMode() == SyncMode.OUTBOX) {
            couponTemplateSearchSyncLogRepository.save(CouponTemplateSearchSyncLog.create("COUPON_TEMPLATE_CHANGED", document));
            return;
        }
        if (searchSyncProperties.getSyncMode() == SyncMode.CANAL) {
            applicationEventPublisher.publishEvent(new CouponTemplateBinlogEvent("UPSERT", document));
            return;
        }
        applicationEventPublisher.publishEvent(new CouponTemplateSearchSyncEvent(document));
    }
}
