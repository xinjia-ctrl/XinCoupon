package com.xinjia.coupon.search.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xinjia.coupon.search.infrastructure.CouponTemplateSearchIndex;
import com.xinjia.coupon.search.sync.CouponTemplateSearchSyncLog;
import com.xinjia.coupon.search.sync.CouponTemplateSearchSyncLogRepository;

@Service
public class CouponTemplateSearchSyncReplayService {

    private final CouponTemplateSearchSyncLogRepository couponTemplateSearchSyncLogRepository;
    private final CouponTemplateSearchIndex couponTemplateSearchIndex;

    public CouponTemplateSearchSyncReplayService(
            CouponTemplateSearchSyncLogRepository couponTemplateSearchSyncLogRepository,
            CouponTemplateSearchIndex couponTemplateSearchIndex
    ) {
        this.couponTemplateSearchSyncLogRepository = couponTemplateSearchSyncLogRepository;
        this.couponTemplateSearchIndex = couponTemplateSearchIndex;
    }

    @Transactional
    public int replay(int limit) {
        int replayLimit = Math.min(Math.max(limit, 1), 500);
        int count = 0;
        for (CouponTemplateSearchSyncLog log : couponTemplateSearchSyncLogRepository.findUnconsumed(replayLimit)) {
            couponTemplateSearchIndex.save(log.getDocument());
            log.markConsumed();
            couponTemplateSearchSyncLogRepository.save(log);
            count++;
        }
        return count;
    }
}
