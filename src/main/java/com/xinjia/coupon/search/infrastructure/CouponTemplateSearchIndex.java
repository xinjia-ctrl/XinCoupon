package com.xinjia.coupon.search.infrastructure;

import java.util.Collection;
import java.util.List;

import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.search.domain.CouponTemplateSearchDocument;

public interface CouponTemplateSearchIndex {

    void save(CouponTemplateSearchDocument document);

    void replaceAll(Collection<CouponTemplateSearchDocument> documents);

    List<CouponTemplateSearchDocument> search(String keyword, Long merchantId, CouponTemplateStatus status);
}
