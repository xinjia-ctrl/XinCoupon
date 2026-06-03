package com.xinjia.coupon.search.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xinjia.coupon.common.api.ApiResponse;
import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.search.application.CouponTemplateSearchService;
import com.xinjia.coupon.search.domain.CouponTemplateSearchDocument;

@RestController
@RequestMapping("/api/search/coupon-templates")
public class CouponTemplateSearchController {

    private final CouponTemplateSearchService couponTemplateSearchService;

    public CouponTemplateSearchController(CouponTemplateSearchService couponTemplateSearchService) {
        this.couponTemplateSearchService = couponTemplateSearchService;
    }

    @GetMapping
    public ApiResponse<List<CouponTemplateSearchDocument>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) CouponTemplateStatus status
    ) {
        return ApiResponse.success(couponTemplateSearchService.search(keyword, merchantId, status));
    }

    @PostMapping("/rebuild")
    public ApiResponse<CouponTemplateSearchRebuildView> rebuild() {
        int indexedCount = couponTemplateSearchService.rebuild();
        return ApiResponse.success(new CouponTemplateSearchRebuildView(indexedCount));
    }
}
