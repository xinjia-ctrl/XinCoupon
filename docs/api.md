# 核心接口文档

## 通用响应

所有接口统一返回 `ApiResponse`。

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": "2026-05-31T10:00:00+08:00"
}
```

## 枚举说明

- `CouponType`：`FULL_REDUCTION`、`DISCOUNT`、`CASH`
- `CouponTemplateStatus`：`DRAFT`、`ENABLED`、`DISABLED`、`EXPIRED`
- `CampaignStatus`：`PENDING`、`RUNNING`、`PAUSED`、`FINISHED`、`CANCELED`
- `UserCouponStatus`：`RECEIVED`、`LOCKED`、`USED`、`EXPIRED`

## 系统接口

### 健康检查

```http
GET /api/system/health
```

## 优惠券模板

### 创建模板

```http
POST /api/admin/coupon-templates
```

```json
{
  "merchantId": 1,
  "title": "新人满减券",
  "couponType": "FULL_REDUCTION",
  "discountAmount": 500,
  "discountRate": null,
  "thresholdAmount": 3000,
  "validStartTime": "2026-06-01T00:00:00+08:00",
  "validEndTime": "2026-06-30T23:59:59+08:00",
  "totalStock": 1000
}
```

### 查询模板

```http
GET /api/admin/coupon-templates/{templateId}
GET /api/admin/coupon-templates
```

### 变更模板状态

```http
PATCH /api/admin/coupon-templates/{templateId}/status
```

```json
{
  "status": "ENABLED"
}
```

## 发券活动

### 创建活动

```http
POST /api/admin/coupon-campaigns
```

```json
{
  "templateId": 1001,
  "merchantId": 1,
  "name": "六月新人发券活动",
  "campaignStock": 500,
  "perUserLimit": 1,
  "startTime": "2026-06-01T00:00:00+08:00",
  "endTime": "2026-06-10T23:59:59+08:00"
}
```

### 查询活动

```http
GET /api/admin/coupon-campaigns/{campaignId}
GET /api/admin/coupon-campaigns
```

### 变更活动状态

```http
PATCH /api/admin/coupon-campaigns/{campaignId}/status
```

```json
{
  "status": "RUNNING"
}
```

## 用户优惠券

### 用户领券

```http
POST /api/user/coupons/receive
```

`requestId` 用于幂等控制，同一个 `requestId` 重试不会重复扣库存。

```json
{
  "requestId": "receive-20260531-0001",
  "userId": 10,
  "campaignId": 2001
}
```

### 查询用户券

```http
GET /api/user/coupons?userId=10
```

## 订单结算

### 试算优惠

```http
POST /api/settlement/calculate
```

```json
{
  "userId": 10,
  "orderNo": "ORDER-20260531-0001",
  "merchantId": 1,
  "orderAmount": 5000,
  "items": [
    {
      "skuId": "SKU-1",
      "categoryCode": "FOOD",
      "quantity": 1,
      "amount": 5000
    }
  ]
}
```

### 锁定优惠券

```http
POST /api/settlement/lock
```

```json
{
  "userId": 10,
  "userCouponId": 3001,
  "orderNo": "ORDER-20260531-0001"
}
```

### 核销优惠券

```http
POST /api/settlement/confirm
```

```json
{
  "userId": 10,
  "userCouponId": 3001,
  "orderNo": "ORDER-20260531-0001"
}
```

### 取消释放优惠券

```http
POST /api/settlement/cancel
```

```json
{
  "userId": 10,
  "userCouponId": 3001,
  "orderNo": "ORDER-20260531-0001"
}
```

## 事件说明

用户领券成功后会发布 `COUPON_RECEIVED` 事件。当前实现使用 Spring 本地事件模拟消息流转，并提供 `eventId` 消费幂等处理；后续可替换为 RocketMQ 生产者和消费者。
