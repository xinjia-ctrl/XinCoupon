# 数据库设计草案

## 设计原则

- 表结构按 XinCoupon 自己的业务模型设计，不复制第三方项目 SQL。
- 金额字段统一使用分为单位的整数，例如 `discount_amount` 表示优惠金额分值。
- 状态字段使用明确枚举值，避免魔法数字散落在代码中。
- 第一阶段先保证单体 MVP 闭环，不引入分库分表。

## 核心表

### coupon_template

优惠券模板表，描述优惠券的基础规则。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `merchant_id` | bigint | 商家 ID |
| `title` | varchar(80) | 优惠券标题 |
| `coupon_type` | varchar(32) | 类型：满减、折扣、立减 |
| `discount_amount` | bigint | 优惠金额，满减和立减使用 |
| `discount_rate` | int | 折扣比例，例如 85 表示 8.5 折 |
| `threshold_amount` | bigint | 使用门槛金额 |
| `valid_start_time` | datetime | 有效期开始时间 |
| `valid_end_time` | datetime | 有效期结束时间 |
| `total_stock` | int | 模板总库存 |
| `status` | varchar(32) | 模板状态 |
| `created_at` | datetime | 创建时间 |
| `updated_at` | datetime | 更新时间 |

建议索引：

- `idx_template_merchant_status`：`merchant_id`、`status`
- `idx_template_valid_time`：`valid_start_time`、`valid_end_time`

### coupon_campaign

发券活动表，描述某个模板在一段时间内的投放规则。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `template_id` | bigint | 优惠券模板 ID |
| `merchant_id` | bigint | 商家 ID |
| `name` | varchar(80) | 活动名称 |
| `campaign_stock` | int | 活动可发库存 |
| `received_count` | int | 已领取数量 |
| `per_user_limit` | int | 单用户领取上限 |
| `start_time` | datetime | 活动开始时间 |
| `end_time` | datetime | 活动结束时间 |
| `status` | varchar(32) | 活动状态 |
| `created_at` | datetime | 创建时间 |
| `updated_at` | datetime | 更新时间 |

建议索引：

- `idx_campaign_template`：`template_id`
- `idx_campaign_status_time`：`status`、`start_time`、`end_time`

### user_coupon

用户优惠券表，保存用户实际领取到的券。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `user_id` | bigint | 用户 ID |
| `template_id` | bigint | 优惠券模板 ID |
| `campaign_id` | bigint | 发券活动 ID |
| `coupon_code` | varchar(64) | 用户券编码 |
| `status` | varchar(32) | 用户券状态 |
| `received_at` | datetime | 领取时间 |
| `locked_at` | datetime | 锁定时间 |
| `used_at` | datetime | 核销时间 |
| `expired_at` | datetime | 过期时间 |
| `order_no` | varchar(64) | 关联订单号 |
| `created_at` | datetime | 创建时间 |
| `updated_at` | datetime | 更新时间 |

建议索引：

- `uk_coupon_code`：`coupon_code`
- `idx_user_coupon_status`：`user_id`、`status`
- `idx_user_campaign`：`user_id`、`campaign_id`

### coupon_receive_record

领券记录表，用于审计、幂等和失败排查。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `request_id` | varchar(64) | 请求幂等 ID |
| `user_id` | bigint | 用户 ID |
| `campaign_id` | bigint | 活动 ID |
| `template_id` | bigint | 模板 ID |
| `result` | varchar(32) | 领取结果 |
| `failure_reason` | varchar(200) | 失败原因 |
| `created_at` | datetime | 创建时间 |

建议索引：

- `uk_receive_request`：`request_id`
- `idx_receive_user_campaign`：`user_id`、`campaign_id`

### coupon_event_log

优惠券事件日志表，用于后续 MQ 消费幂等和问题追踪。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `event_id` | varchar(64) | 事件唯一 ID |
| `event_type` | varchar(64) | 事件类型 |
| `biz_id` | varchar(64) | 业务 ID |
| `payload` | text | 事件内容 |
| `consume_status` | varchar(32) | 消费状态 |
| `created_at` | datetime | 创建时间 |
| `updated_at` | datetime | 更新时间 |

建议索引：

- `uk_event_id`：`event_id`
- `idx_event_type_status`：`event_type`、`consume_status`

## 状态流转

### 优惠券模板

```text
DRAFT -> ENABLED -> DISABLED
ENABLED -> EXPIRED
```

### 发券活动

```text
PENDING -> RUNNING -> FINISHED
RUNNING -> PAUSED -> RUNNING
RUNNING -> CANCELED
```

### 用户优惠券

```text
RECEIVED -> LOCKED -> USED
LOCKED -> RECEIVED
RECEIVED -> EXPIRED
```

## 后续落库计划

第 3 天实现模板管理时，优先创建 `coupon_template` 对应实体和仓储层。第 4 天实现活动管理时，再补充 `coupon_campaign`。用户券、领券记录和事件日志跟随后续业务提交逐步落地。
