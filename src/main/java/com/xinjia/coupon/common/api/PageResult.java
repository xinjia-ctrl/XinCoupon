package com.xinjia.coupon.common.api;

import java.util.List;

public record PageResult<T>(
        List<T> records,
        long total,
        int pageNo,
        int pageSize
) {
}
