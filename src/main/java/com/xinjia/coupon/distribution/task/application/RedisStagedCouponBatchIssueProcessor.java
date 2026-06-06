package com.xinjia.coupon.distribution.task.application;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.xinjia.coupon.admin.campaign.application.CouponCampaignService;
import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.dispatch.event.application.CouponEventPublisher;
import com.xinjia.coupon.dispatch.event.domain.CouponReceivedEvent;
import com.xinjia.coupon.distribution.task.domain.CouponBatchTask;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;
import com.xinjia.coupon.user.coupon.infrastructure.ReceiveRequestRepository;
import com.xinjia.coupon.user.coupon.infrastructure.StockDeductStatus;
import com.xinjia.coupon.user.coupon.infrastructure.UserCouponRepository;

@Service
public class RedisStagedCouponBatchIssueProcessor implements CouponBatchIssueProcessor {

    private final CouponCampaignService couponCampaignService;
    private final CouponTemplateService couponTemplateService;
    private final UserCouponRepository userCouponRepository;
    private final ReceiveRequestRepository receiveRequestRepository;
    private final CouponEventPublisher couponEventPublisher;
    private final CouponBatchUserStagingStore couponBatchUserStagingStore;

    public RedisStagedCouponBatchIssueProcessor(
            CouponCampaignService couponCampaignService,
            CouponTemplateService couponTemplateService,
            UserCouponRepository userCouponRepository,
            ReceiveRequestRepository receiveRequestRepository,
            CouponEventPublisher couponEventPublisher,
            CouponBatchUserStagingStore couponBatchUserStagingStore
    ) {
        this.couponCampaignService = couponCampaignService;
        this.couponTemplateService = couponTemplateService;
        this.userCouponRepository = userCouponRepository;
        this.receiveRequestRepository = receiveRequestRepository;
        this.couponEventPublisher = couponEventPublisher;
        this.couponBatchUserStagingStore = couponBatchUserStagingStore;
    }

    @Override
    public BatchIssueResult issue(CouponBatchTask task, List<BatchUserRow> rows) {
        couponCampaignService.ensureReceivable(task.getCampaignId());
        CouponCampaign campaign = couponCampaignService.getById(task.getCampaignId());
        CouponTemplate template = couponTemplateService.getById(campaign.getTemplateId());

        int successCount = 0;
        List<BatchIssueFailure> failures = new ArrayList<>();
        for (BatchUserRow row : rows) {
            BatchStageResult stageResult = couponBatchUserStagingStore.stage(
                    task.getId(),
                    campaign.getId(),
                    row,
                    campaign.getPerUserLimit()
            );
            if (!stageResult.success()) {
                failures.add(new BatchIssueFailure(row.userId(), row.rowNumber(), stageFailureReason(stageResult.status())));
                continue;
            }
            if (stageResult.stagedSize() >= CouponBatchTaskService.BATCH_SIZE) {
                FlushResult flushResult = flush(task, campaign, template, CouponBatchTaskService.BATCH_SIZE);
                successCount += flushResult.successCount();
                failures.addAll(flushResult.failures());
            }
        }

        while (couponBatchUserStagingStore.size(task.getId()) > 0) {
            FlushResult flushResult = flush(task, campaign, template, CouponBatchTaskService.BATCH_SIZE);
            if (flushResult.successCount() == 0 && flushResult.failures().isEmpty()) {
                break;
            }
            successCount += flushResult.successCount();
            failures.addAll(flushResult.failures());
        }
        couponBatchUserStagingStore.clear(task.getId());
        return new BatchIssueResult(successCount, failures);
    }

    private FlushResult flush(CouponBatchTask task, CouponCampaign campaign, CouponTemplate template, int count) {
        List<BatchUserRow> stagedRows = couponBatchUserStagingStore.pop(task.getId(), count);
        if (stagedRows.isEmpty()) {
            return FlushResult.empty();
        }

        List<BatchIssueFailure> failures = new ArrayList<>();
        List<BatchUserRow> eligibleRows = new ArrayList<>(stagedRows.size());
        for (BatchUserRow row : stagedRows) {
            if (userCouponRepository.countByUserIdAndCampaignId(row.userId(), campaign.getId()) >= campaign.getPerUserLimit()) {
                failures.add(new BatchIssueFailure(row.userId(), row.rowNumber(), "用户已达到该活动领取上限"));
                couponBatchUserStagingStore.restoreReservation(campaign.getId(), row.userId());
                continue;
            }
            eligibleRows.add(row);
        }

        if (eligibleRows.isEmpty()) {
            return new FlushResult(0, failures);
        }

        try {
            couponCampaignService.deductDatabaseStock(campaign.getId(), eligibleRows.size());
        } catch (RuntimeException exception) {
            for (BatchUserRow row : eligibleRows) {
                failures.add(new BatchIssueFailure(row.userId(), row.rowNumber(), exception.getMessage()));
                couponBatchUserStagingStore.restoreReservation(campaign.getId(), row.userId());
            }
            return new FlushResult(0, failures);
        }

        List<UserCoupon> userCoupons = eligibleRows.stream()
                .map(row -> UserCoupon.receive(row.userId(), template.getId(), campaign.getId(), template.getValidEndTime()))
                .toList();
        try {
            List<UserCoupon> savedCoupons = userCouponRepository.saveBatch(userCoupons);
            for (UserCoupon savedCoupon : savedCoupons) {
                afterUserCouponSaved(task, savedCoupon);
            }
            return new FlushResult(savedCoupons.size(), failures);
        } catch (RuntimeException exception) {
            return fallbackSaveOneByOne(task, campaign, eligibleRows, userCoupons, failures);
        }
    }

    private FlushResult fallbackSaveOneByOne(
            CouponBatchTask task,
            CouponCampaign campaign,
            List<BatchUserRow> rows,
            List<UserCoupon> userCoupons,
            List<BatchIssueFailure> failures
    ) {
        int successCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            BatchUserRow row = rows.get(i);
            try {
                if (userCouponRepository.countByUserIdAndCampaignId(row.userId(), campaign.getId()) >= campaign.getPerUserLimit()) {
                    failures.add(new BatchIssueFailure(row.userId(), row.rowNumber(), "用户已达到该活动领取上限"));
                    restoreReservedStock(campaign.getId(), row.userId());
                    continue;
                }
                UserCoupon savedCoupon = userCouponRepository.save(userCoupons.get(i));
                afterUserCouponSaved(task, savedCoupon);
                successCount++;
            } catch (RuntimeException singleException) {
                failures.add(new BatchIssueFailure(row.userId(), row.rowNumber(), singleException.getMessage()));
                restoreReservedStock(campaign.getId(), row.userId());
            }
        }
        return new FlushResult(successCount, failures);
    }

    private void restoreReservedStock(Long campaignId, Long userId) {
        couponCampaignService.restoreDatabaseStock(campaignId);
        couponBatchUserStagingStore.restoreReservation(campaignId, userId);
    }

    private void afterUserCouponSaved(CouponBatchTask task, UserCoupon userCoupon) {
        receiveRequestRepository.saveResult(task.getBatchNo() + "-" + userCoupon.getUserId(), userCoupon);
        couponEventPublisher.publish(new CouponReceivedEvent(
                UUID.randomUUID().toString(),
                userCoupon.getUserId(),
                userCoupon.getId(),
                userCoupon.getTemplateId(),
                userCoupon.getCampaignId(),
                OffsetDateTime.now()
        ));
    }

    private String stageFailureReason(StockDeductStatus status) {
        return switch (status) {
            case STOCK_NOT_FOUND -> "活动库存未预热";
            case STOCK_EMPTY -> "活动库存不足";
            case RECEIVE_LIMIT_EXCEEDED -> "用户已达到该活动领取上限";
            case SUCCESS -> "领取失败";
        };
    }

    private record FlushResult(int successCount, List<BatchIssueFailure> failures) {

        private static FlushResult empty() {
            return new FlushResult(0, List.of());
        }
    }
}
