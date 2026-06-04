package com.xinjia.coupon.search.sync;

import java.util.List;

public interface CouponTemplateSearchSyncLogRepository {

    CouponTemplateSearchSyncLog save(CouponTemplateSearchSyncLog log);

    List<CouponTemplateSearchSyncLog> findUnconsumed(int limit);
}
