package com.xinjia.coupon.user.coupon.infrastructure;

public enum StockDeductStatus {

    SUCCESS(0),
    STOCK_NOT_FOUND(1),
    STOCK_EMPTY(2),
    RECEIVE_LIMIT_EXCEEDED(3);

    private final int code;

    StockDeductStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static StockDeductStatus fromCode(int code) {
        for (StockDeductStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown stock deduct status code: " + code);
    }
}
