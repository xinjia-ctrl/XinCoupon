package com.xinjia.coupon.common.idempotent;

import java.time.Duration;

public interface DuplicateSubmitStore {

    boolean markIfAbsent(String key, Duration ttl);
}
