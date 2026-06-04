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

## 鉴权说明

默认 `AUTH_ENABLED=false`，接口可以继续通过请求体或查询参数传递 `userId`，便于本地联调。

开启 `AUTH_ENABLED=true` 后：

- 管理端接口 `/api/admin/**` 需要请求头 `X-Admin-Token`。
- 用户券接口 `/api/user/**` 和结算接口 `/api/settlement/**` 需要请求头 `X-User-Id`。
- `GET /api/system/health` 不需要鉴权。

示例：

```http
GET /api/user/coupons
X-User-Id: 10
```

## 幂等与重复提交

管理端创建优惠券模板和创建批量发券任务已接入 `@NoDuplicateSubmit` 防重复提交。当前默认使用本地内存存储实现，后续可以替换为 Redis/Redisson 分布式实现。

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

### 增加模板发行量

```http
POST /api/admin/coupon-templates/{templateId}/stock/increase
```

```json
{
  "increasedStock": 100
}
```

### 终止模板

```http
POST /api/admin/coupon-templates/{templateId}/terminate
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

## 批量发券任务

### 创建批量发券任务

```http
POST /api/admin/coupon-batch-tasks
```

当前请求体直接传入用户 ID 列表，用于模拟 Excel 导入后的用户清单；服务会异步执行发券。

```json
{
  "batchNo": "batch-20260603-0001",
  "campaignId": 2001,
  "userIds": [10, 11, 12]
}
```

### 查询批量发券任务

```http
GET /api/admin/coupon-batch-tasks/{taskId}
```

### 分页查询批量发券任务

```http
GET /api/admin/coupon-batch-tasks?pageNo=1&pageSize=10&status=PARTIAL_FAILED
```

### 查询批量发券失败明细

```http
GET /api/admin/coupon-batch-tasks/{taskId}/failures
```

## 优惠券预约提醒

### 创建预约提醒

```http
POST /api/engine/coupon-template-reminds
```

```json
{
  "userId": 10,
  "templateId": 1001,
  "remindType": "APP",
  "remindAt": "2026-06-05T09:50:00+08:00"
}
```

### 查询预约提醒

```http
GET /api/engine/coupon-template-reminds?userId=10&status=ACTIVE
```

### 取消预约提醒

```http
POST /api/engine/coupon-template-reminds/cancel
```

```json
{
  "userId": 10,
  "remindId": 9001
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
  "campaignId": 2001
}
```

### 用户异步领券

```http
POST /api/user/coupons/receive-mq
```

默认通过本地事件异步处理；开启 `RECEIVE_ROCKETMQ_ENABLED=true` 后通过 RocketMQ 投递领券请求。

```json
{
  "requestId": "receive-mq-20260604-0001",
  "campaignId": 2001
}
```

### 查询用户券

```http
GET /api/user/coupons
```

## 订单结算

### 试算优惠

```http
POST /api/settlement/calculate
```

```json
{
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
  "userCouponId": 3001,
  "orderNo": "ORDER-20260531-0001"
}
```

### 退款返还优惠券

```http
POST /api/settlement/refund
```

```json
{
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
  "userCouponId": 3001,
  "orderNo": "ORDER-20260531-0001"
}
```

## 搜索接口

### 搜索优惠券模板

```http
GET /api/search/coupon-templates?keyword=新人&merchantId=1&status=ENABLED
```

`keyword`、`merchantId`、`status` 都是可选参数。当前实现使用内存索引模拟 Elasticsearch 文档，模板创建和状态变更后会通过应用事件同步索引。

### 重建搜索索引

```http
POST /api/search/coupon-templates/rebuild
```

用于模拟从数据库或 Binlog 重新构建搜索索引。

### 重放搜索同步事件

```http
POST /api/search/coupon-templates/sync-events/replay?limit=100
```

当 `SEARCH_SYNC_MODE=OUTBOX` 时，模板变更会先写入同步日志，调用该接口可重放未消费事件并刷新搜索索引。

## 高级配置

### 搜索同步模式

- `SEARCH_SYNC_MODE=APPLICATION_EVENT`：默认模式，模板变更后直接通过应用事件刷新索引。
- `SEARCH_SYNC_MODE=OUTBOX`：模板变更写入同步日志，再通过重放接口刷新索引。
- `SEARCH_SYNC_MODE=CANAL`：通过 Binlog 事件模型模拟 Canal 同步刷新索引。

### 幂等和分布式锁

- `IDEMPOTENT_STORE_TYPE=memory|redis`：管理端防重复提交存储。
- `MQ_IDEMPOTENT_STORE=mysql|redis`：MQ 消费幂等存储。
- `DISTRIBUTED_LOCK_TYPE=memory|redis`：结算单状态迁移锁。

### 分片

`SHARDING_ENABLED=true` 后，用户券 MyBatis 操作会通过动态表名路由到 `user_coupon_N`。本地未创建分表时保持默认关闭。

## 事件说明

用户领券成功后会发布 `COUPON_RECEIVED` 事件。当前实现使用 Spring 本地事件模拟消息流转，并提供 `eventId` 消费幂等处理；后续可替换为 RocketMQ 生产者和消费者。
