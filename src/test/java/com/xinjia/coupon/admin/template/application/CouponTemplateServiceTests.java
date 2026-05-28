package com.xinjia.coupon.admin.template.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.admin.template.infrastructure.InMemoryCouponTemplateRepository;
import com.xinjia.coupon.admin.template.web.CreateCouponTemplateRequest;
import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.common.enums.CouponType;
import com.xinjia.coupon.common.exception.BusinessException;

class CouponTemplateServiceTests {

    private CouponTemplateService couponTemplateService;

    @BeforeEach
    void setUp() {
        couponTemplateService = new CouponTemplateService(new InMemoryCouponTemplateRepository());
    }

    @Test
    void createShouldSaveDraftTemplate() {
        CouponTemplate template = couponTemplateService.create(validFullReductionRequest());

        assertThat(template.getId()).isNotNull();
        assertThat(template.getStatus()).isEqualTo(CouponTemplateStatus.DRAFT);
        assertThat(template.getTitle()).isEqualTo("新人满减券");
    }

    @Test
    void createShouldRejectInvalidTimeRange() {
        CreateCouponTemplateRequest request = new CreateCouponTemplateRequest(
                1L,
                "时间错误券",
                CouponType.CASH,
                500L,
                null,
                0L,
                OffsetDateTime.now().plusDays(3),
                OffsetDateTime.now().plusDays(1),
                100
        );

        assertThatThrownBy(() -> couponTemplateService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("优惠券有效结束时间必须晚于开始时间");
    }

    private CreateCouponTemplateRequest validFullReductionRequest() {
        return new CreateCouponTemplateRequest(
                1L,
                "新人满减券",
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
