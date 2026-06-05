package com.xinjia.coupon.distribution.task.web;

import com.alibaba.excel.annotation.ExcelProperty;

public class CouponBatchTaskExcelRow {

    @ExcelProperty("userId")
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
