package com.xinjia.coupon.common.persistence;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("coupon_operation_log")
public class CouponOperationLogDO extends BaseCreateTimeEntity {

    private String bizType;
    private String bizId;
    private String operationType;
    private Long operatorId;
    private String operationDetail;

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperationDetail() {
        return operationDetail;
    }

    public void setOperationDetail(String operationDetail) {
        this.operationDetail = operationDetail;
    }
}
