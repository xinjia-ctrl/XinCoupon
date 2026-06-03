package com.xinjia.coupon.search.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.search.domain.CouponTemplateSearchDocument;
import com.xinjia.coupon.search.infrastructure.CouponTemplateSearchIndex;

@Service
public class CouponTemplateSearchService {

    private final CouponTemplateSearchIndex couponTemplateSearchIndex;
    private final CouponTemplateService couponTemplateService;

    public CouponTemplateSearchService(
            CouponTemplateSearchIndex couponTemplateSearchIndex,
            CouponTemplateService couponTemplateService
    ) {
        this.couponTemplateSearchIndex = couponTemplateSearchIndex;
        this.couponTemplateService = couponTemplateService;
    }

    @Transactional(readOnly = true)
    public List<CouponTemplateSearchDocument> search(
            String keyword,
            Long merchantId,
            CouponTemplateStatus status
    ) {
        return couponTemplateSearchIndex.search(normalize(keyword), merchantId, status);
    }

    @Transactional(readOnly = true)
    public int rebuild() {
        List<CouponTemplate> templates = couponTemplateService.list();
        couponTemplateSearchIndex.replaceAll(
                templates.stream()
                        .map(CouponTemplateSearchDocument::from)
                        .toList()
        );
        return templates.size();
    }

    private String normalize(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }
}
