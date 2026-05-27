package com.xinjia.coupon.common.enums;

public enum ErrorCode {
    PARAMETER_INVALID(40001, "请求参数不合法"),
    RESOURCE_NOT_FOUND(40401, "资源不存在"),
    BUSINESS_REJECTED(40901, "业务规则校验失败"),
    SYSTEM_ERROR(50000, "系统繁忙，请稍后重试");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
