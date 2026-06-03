package com.xinjia.coupon.common.auth;

import java.util.Optional;

public final class RequestIdentityHolder {

    private static final ThreadLocal<RequestIdentity> CURRENT = new ThreadLocal<>();

    private RequestIdentityHolder() {
    }

    public static void set(RequestIdentity identity) {
        CURRENT.set(identity);
    }

    public static Optional<RequestIdentity> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static Optional<Long> getUserId() {
        return get().map(RequestIdentity::userId);
    }

    public static void clear() {
        CURRENT.remove();
    }
}
