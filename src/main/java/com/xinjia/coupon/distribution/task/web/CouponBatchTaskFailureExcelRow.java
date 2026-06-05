package com.xinjia.coupon.distribution.task.web;

import com.alibaba.excel.annotation.ExcelProperty;
import com.xinjia.coupon.distribution.task.domain.CouponBatchTaskFailure;

public class CouponBatchTaskFailureExcelRow {

    @ExcelProperty("batchNo")
    private String batchNo;

    @ExcelProperty("userId")
    private Long userId;

    @ExcelProperty("rowNumber")
    private Integer rowNumber;

    @ExcelProperty("failureReason")
    private String failureReason;

    public static CouponBatchTaskFailureExcelRow from(CouponBatchTaskFailure failure) {
        CouponBatchTaskFailureExcelRow row = new CouponBatchTaskFailureExcelRow();
        row.batchNo = failure.getBatchNo();
        row.userId = failure.getUserId();
        row.rowNumber = failure.getRowNumber();
        row.failureReason = failure.getFailureReason();
        return row;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
