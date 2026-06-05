package com.xinjia.coupon.search.sync;

import com.xinjia.coupon.search.domain.CouponTemplateSearchDocument;

public record CouponTemplateBinlogEvent(
        String operation,
        Long templateId,
        CouponTemplateSearchDocument document
) {

    public static CouponTemplateBinlogEvent upsert(CouponTemplateSearchDocument document) {
        return new CouponTemplateBinlogEvent("UPSERT", document.templateId(), document);
    }

    public static CouponTemplateBinlogEvent delete(Long templateId) {
        return new CouponTemplateBinlogEvent("DELETE", templateId, null);
    }
}
