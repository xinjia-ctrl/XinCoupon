package com.xinjia.coupon.common.auth;

import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.exception.BusinessException;

public final class RequestIdentityResolver {

    private RequestIdentityResolver() {
    }

    public static Long resolveUserId(Long fallbackUserId) {
        return RequestIdentityHolder.getUserId()
                .orElseGet(() -> {
                    if (fallbackUserId == null) {
                        throw new BusinessException(ErrorCode.PARAMETER_INVALID, "缺少用户身份");
                    }
                    return fallbackUserId;
                });
    }
}
