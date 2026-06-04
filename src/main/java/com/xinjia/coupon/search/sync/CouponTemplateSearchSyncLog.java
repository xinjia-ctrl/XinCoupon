package com.xinjia.coupon.search.sync;

import java.time.OffsetDateTime;

import com.xinjia.coupon.search.domain.CouponTemplateSearchDocument;

public class CouponTemplateSearchSyncLog {

    private Long id;
    private String eventType;
    private CouponTemplateSearchDocument document;
    private boolean consumed;
    private OffsetDateTime createdAt;
    private OffsetDateTime consumedAt;

    public static CouponTemplateSearchSyncLog create(String eventType, CouponTemplateSearchDocument document) {
        CouponTemplateSearchSyncLog log = new CouponTemplateSearchSyncLog();
        log.eventType = eventType;
        log.document = document;
        log.consumed = false;
        log.createdAt = OffsetDateTime.now();
        return log;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void markConsumed() {
        this.consumed = true;
        this.consumedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public CouponTemplateSearchDocument getDocument() {
        return document;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getConsumedAt() {
        return consumedAt;
    }
}
