package com.xinjia.coupon.engine.remind.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.admin.template.infrastructure.InMemoryCouponTemplateRepository;
import com.xinjia.coupon.admin.template.web.CreateCouponTemplateRequest;
import com.xinjia.coupon.common.enums.CouponTemplateRemindStatus;
import com.xinjia.coupon.common.enums.CouponType;
import com.xinjia.coupon.common.exception.BusinessException;
import com.xinjia.coupon.engine.remind.domain.CouponTemplateRemind;
import com.xinjia.coupon.engine.remind.infrastructure.InMemoryCouponTemplateRemindRepository;
import com.xinjia.coupon.engine.remind.web.CancelCouponTemplateRemindRequest;
import com.xinjia.coupon.engine.remind.web.CreateCouponTemplateRemindRequest;

class CouponTemplateRemindServiceTests {

    private CouponTemplateService couponTemplateService;
    private CouponTemplateRemindService couponTemplateRemindService;

    @BeforeEach
    void setUp() {
        couponTemplateService = new CouponTemplateService(new InMemoryCouponTemplateRepository());
        couponTemplateRemindService = new CouponTemplateRemindService(
                new InMemoryCouponTemplateRemindRepository(),
                couponTemplateService
        );
    }

    @Test
    void createShouldSaveActiveRemindBeforeTemplateValidStartTime() {
        CouponTemplate template = createTemplate();

        CouponTemplateRemind remind = couponTemplateRemindService.create(new CreateCouponTemplateRemindRequest(
                10L,
                template.getId(),
                "APP",
                OffsetDateTime.now().plusHours(12)
        ));

        assertThat(remind.getId()).isNotNull();
        assertThat(remind.getStatus()).isEqualTo(CouponTemplateRemindStatus.ACTIVE);
        assertThat(couponTemplateRemindService.list(10L, CouponTemplateRemindStatus.ACTIVE)).hasSize(1);
    }

    @Test
    void cancelShouldMarkRemindCanceled() {
        CouponTemplate template = createTemplate();
        CouponTemplateRemind remind = couponTemplateRemindService.create(new CreateCouponTemplateRemindRequest(
                10L,
                template.getId(),
                "APP",
                OffsetDateTime.now().plusHours(12)
        ));

        CouponTemplateRemind canceled = couponTemplateRemindService.cancel(new CancelCouponTemplateRemindRequest(
                10L,
                remind.getId()
        ));

        assertThat(canceled.getStatus()).isEqualTo(CouponTemplateRemindStatus.CANCELED);
    }

    @Test
    void createShouldRejectDuplicateActiveRemind() {
        CouponTemplate template = createTemplate();
        CreateCouponTemplateRemindRequest request = new CreateCouponTemplateRemindRequest(
                10L,
                template.getId(),
                "APP",
                OffsetDateTime.now().plusHours(12)
        );
        couponTemplateRemindService.create(request);

        assertThatThrownBy(() -> couponTemplateRemindService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("该优惠券模板已存在有效预约提醒");
    }

    private CouponTemplate createTemplate() {
        return couponTemplateService.create(new CreateCouponTemplateRequest(
                1L,
                "预约提醒券",
                CouponType.FULL_REDUCTION,
                500L,
                null,
                3000L,
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(30),
                1000
        ));
    }
}
