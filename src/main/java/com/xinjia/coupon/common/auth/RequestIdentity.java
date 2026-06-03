package com.xinjia.coupon.common.auth;

public record RequestIdentity(
        Long userId,
        boolean admin
) {

    public static RequestIdentity user(Long userId) {
        return new RequestIdentity(userId, false);
    }

    public static RequestIdentity administrator() {
        return new RequestIdentity(null, true);
    }
}
