package com.xinjia.coupon.user.coupon.infrastructure;

public final class StockDecrementReturnCombinedUtil {

    private static final long LOW_32_BITS_MASK = 0xFFFF_FFFFL;
    private static final int STATUS_SHIFT = 32;

    private StockDecrementReturnCombinedUtil() {
    }

    public static long combine(StockDeductStatus status, long receiveCount) {
        return ((long) status.getCode() << STATUS_SHIFT) | (receiveCount & LOW_32_BITS_MASK);
    }

    public static StockDeductResult parse(long combined) {
        int statusCode = (int) (combined >>> STATUS_SHIFT);
        long receiveCount = combined & LOW_32_BITS_MASK;
        return new StockDeductResult(StockDeductStatus.fromCode(statusCode), receiveCount);
    }
}
