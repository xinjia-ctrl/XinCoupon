package com.xinjia.coupon.search.sync;

import com.xinjia.coupon.search.domain.CouponTemplateSearchDocument;

public record CouponTemplateSearchSyncEvent(
        CouponTemplateSearchDocument document
) {
}
