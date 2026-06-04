package com.xinjia.coupon.admin.template.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.admin.template.infrastructure.InMemoryCouponTemplateRepository;
import com.xinjia.coupon.admin.template.web.CreateCouponTemplateRequest;
import com.xinjia.coupon.admin.template.web.IncreaseCouponTemplateStockRequest;
import com.xinjia.coupon.admin.template.web.UpdateCouponTemplateStatusRequest;
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

    @Test
    void getByIdShouldReturnTemplate() {
        CouponTemplate saved = couponTemplateService.create(validFullReductionRequest());

        CouponTemplate template = couponTemplateService.getById(saved.getId());

        assertThat(template.getId()).isEqualTo(saved.getId());
        assertThat(template.getTitle()).isEqualTo("新人满减券");
    }

    @Test
    void listShouldReturnCreatedTemplates() {
        couponTemplateService.create(validFullReductionRequest());

        assertThat(couponTemplateService.list()).hasSize(1);
    }

    @Test
    void changeStatusShouldUpdateTemplateStatus() {
        CouponTemplate saved = couponTemplateService.create(validFullReductionRequest());

        CouponTemplate template = couponTemplateService.changeStatus(
                saved.getId(),
                new UpdateCouponTemplateStatusRequest(CouponTemplateStatus.ENABLED)
        );

        assertThat(template.getStatus()).isEqualTo(CouponTemplateStatus.ENABLED);
    }

    @Test
    void increaseStockShouldAddTemplateStock() {
        CouponTemplate saved = couponTemplateService.create(validFullReductionRequest());

        CouponTemplate template = couponTemplateService.increaseStock(
                saved.getId(),
                new IncreaseCouponTemplateStockRequest(200)
        );

        assertThat(template.getTotalStock()).isEqualTo(1200);
    }

    @Test
    void terminateShouldDisableTemplate() {
        CouponTemplate saved = couponTemplateService.create(validFullReductionRequest());

        CouponTemplate template = couponTemplateService.terminate(saved.getId());

        assertThat(template.getStatus()).isEqualTo(CouponTemplateStatus.DISABLED);
    }

    @Test
    void getByIdShouldRejectMissingTemplate() {
        assertThatThrownBy(() -> couponTemplateService.getById(404L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("优惠券模板不存在");
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
