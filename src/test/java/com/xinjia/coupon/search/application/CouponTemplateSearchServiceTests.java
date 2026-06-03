package com.xinjia.coupon.search.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.admin.template.infrastructure.InMemoryCouponTemplateRepository;
import com.xinjia.coupon.admin.template.web.CreateCouponTemplateRequest;
import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.common.enums.CouponType;
import com.xinjia.coupon.search.domain.CouponTemplateSearchDocument;
import com.xinjia.coupon.search.infrastructure.InMemoryCouponTemplateSearchIndex;

class CouponTemplateSearchServiceTests {

    private CouponTemplateSearchService couponTemplateSearchService;
    private InMemoryCouponTemplateSearchIndex searchIndex;

    @BeforeEach
    void setUp() {
        InMemoryCouponTemplateRepository templateRepository = new InMemoryCouponTemplateRepository();
        CouponTemplateService couponTemplateService = new CouponTemplateService(templateRepository);
        searchIndex = new InMemoryCouponTemplateSearchIndex();
        couponTemplateSearchService = new CouponTemplateSearchService(searchIndex, couponTemplateService);
        couponTemplateService.create(templateRequest("新人满减券", 1L));
        couponTemplateService.create(templateRequest("会员生日券", 2L));
    }

    @Test
    void rebuildShouldIndexAllTemplates() {
        int indexedCount = couponTemplateSearchService.rebuild();

        assertThat(indexedCount).isEqualTo(2);
        assertThat(couponTemplateSearchService.search(null, null, null)).hasSize(2);
    }

    @Test
    void searchShouldFilterByKeywordAndMerchant() {
        couponTemplateSearchService.rebuild();

        assertThat(couponTemplateSearchService.search("新人", 1L, null))
                .extracting(CouponTemplateSearchDocument::title)
                .containsExactly("新人满减券");
    }

    @Test
    void searchIndexShouldHandleTemplateChangeEventDocument() {
        CouponTemplate template = CouponTemplate.create(
                3L,
                "活动同步券",
                CouponType.CASH,
                100L,
                null,
                0L,
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(10),
                100
        );
        template.assignId(3001L);

        searchIndex.save(CouponTemplateSearchDocument.from(template));

        assertThat(couponTemplateSearchService.search("同步", null, CouponTemplateStatus.DRAFT))
                .extracting(CouponTemplateSearchDocument::templateId)
                .containsExactly(3001L);
    }

    private CreateCouponTemplateRequest templateRequest(String title, Long merchantId) {
        return new CreateCouponTemplateRequest(
                merchantId,
                title,
                CouponType.FULL_REDUCTION,
                500L,
                null,
                3000L,
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(30),
                1000
        );
    }
}
