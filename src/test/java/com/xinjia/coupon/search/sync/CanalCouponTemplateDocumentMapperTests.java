package com.xinjia.coupon.search.sync;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.common.enums.CouponType;
import com.xinjia.coupon.search.domain.CouponTemplateSearchDocument;

class CanalCouponTemplateDocumentMapperTests {

    @Test
    void shouldMapCanalRowToSearchDocument() {
        CanalCouponTemplateDocumentMapper mapper = new CanalCouponTemplateDocumentMapper();

        CouponTemplateSearchDocument document = mapper.fromRow(Map.ofEntries(
                Map.entry("id", "1001"),
                Map.entry("merchant_id", "10"),
                Map.entry("title", "新人券"),
                Map.entry("coupon_type", "FULL_REDUCTION"),
                Map.entry("threshold_amount", "3000"),
                Map.entry("discount_amount", "500"),
                Map.entry("discount_rate", ""),
                Map.entry("status", "ENABLED"),
                Map.entry("valid_start_time", "2026-06-01 00:00:00"),
                Map.entry("valid_end_time", "2026-06-30 23:59:59"),
                Map.entry("updated_at", "2026-06-05 21:00:00")
        ));

        assertThat(document.templateId()).isEqualTo(1001L);
        assertThat(document.merchantId()).isEqualTo(10L);
        assertThat(document.couponType()).isEqualTo(CouponType.FULL_REDUCTION);
        assertThat(document.status()).isEqualTo(CouponTemplateStatus.ENABLED);
        assertThat(document.discountRate()).isNull();
    }
}
