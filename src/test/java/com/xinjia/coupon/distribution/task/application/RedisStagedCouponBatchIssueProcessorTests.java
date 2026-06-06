package com.xinjia.coupon.distribution.task.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.xinjia.coupon.admin.campaign.application.CouponCampaignService;
import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.enums.CampaignStatus;
import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.common.enums.CouponType;
import com.xinjia.coupon.dispatch.event.application.CouponEventPublisher;
import com.xinjia.coupon.distribution.task.domain.CouponBatchTask;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;
import com.xinjia.coupon.user.coupon.infrastructure.InMemoryUserCouponRepository;
import com.xinjia.coupon.user.coupon.infrastructure.ReceiveRequestRepository;

class RedisStagedCouponBatchIssueProcessorTests {

    @Test
    void issueShouldFallbackToOneByOneWhenBatchSaveFails() {
        CouponCampaignService campaignService = mock(CouponCampaignService.class);
        CouponTemplateService templateService = mock(CouponTemplateService.class);
        ReceiveRequestRepository receiveRequestRepository = mock(ReceiveRequestRepository.class);
        CouponEventPublisher couponEventPublisher = mock(CouponEventPublisher.class);
        InMemoryCouponBatchUserStagingStore stagingStore = new InMemoryCouponBatchUserStagingStore();
        InMemoryUserCouponRepository userCouponRepository = new InMemoryUserCouponRepository() {
            @Override
            public List<UserCoupon> saveBatch(List<UserCoupon> userCoupons) {
                throw new IllegalStateException("批量写入失败");
            }

            @Override
            public UserCoupon save(UserCoupon userCoupon) {
                if (userCoupon.getUserId().equals(11L)) {
                    throw new IllegalStateException("单条写入失败");
                }
                return super.save(userCoupon);
            }
        };
        RedisStagedCouponBatchIssueProcessor processor = new RedisStagedCouponBatchIssueProcessor(
                campaignService,
                templateService,
                userCouponRepository,
                receiveRequestRepository,
                couponEventPublisher,
                stagingStore
        );
        CouponCampaign campaign = runningCampaign();
        CouponTemplate template = template();
        when(campaignService.getById(2001L)).thenReturn(campaign);
        when(templateService.getById(1001L)).thenReturn(template);
        CouponBatchTask task = CouponBatchTask.create("batch-redis", 2001L, 2);
        task.assignId(5001L);

        BatchIssueResult result = processor.issue(task, List.of(
                new BatchUserRow(10L, 1),
                new BatchUserRow(11L, 2)
        ));

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failures())
                .singleElement()
                .satisfies(failure -> {
                    assertThat(failure.userId()).isEqualTo(11L);
                    assertThat(failure.rowNumber()).isEqualTo(2);
                    assertThat(failure.reason()).isEqualTo("单条写入失败");
                });
        verify(campaignService).deductDatabaseStock(2001L, 2);
        verify(campaignService).restoreDatabaseStock(2001L);
    }

    private CouponCampaign runningCampaign() {
        OffsetDateTime now = OffsetDateTime.now();
        return CouponCampaign.restore(
                2001L,
                1001L,
                1L,
                "批量发券活动",
                100,
                100,
                0,
                1,
                now.minusHours(1),
                now.plusHours(1),
                CampaignStatus.RUNNING,
                now,
                now
        );
    }

    private CouponTemplate template() {
        OffsetDateTime now = OffsetDateTime.now();
        return CouponTemplate.restore(
                1001L,
                1L,
                "批量发券模板",
                CouponType.CASH,
                500L,
                null,
                0L,
                now.minusHours(1),
                now.plusDays(1),
                100,
                CouponTemplateStatus.ENABLED,
                now,
                now
        );
    }
}
