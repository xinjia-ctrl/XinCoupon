# XinCoupon MVP 需求说明

## 项目定位

XinCoupon 是一个面向学习和作品展示的优惠券系统 MVP。当前阶段采用单体 Spring Boot 应用完成核心业务闭环，并按功能风险逐步引入 MySQL、Redis、RocketMQ、Nacos、Knife4j 和请求头鉴权能力。

## 目标用户

- 商家：创建优惠券模板，配置发券活动，查看活动状态。
- 用户：领取优惠券，查询自己的优惠券，在订单结算时使用优惠券。
- 平台维护者：查看系统运行状态，维护基础配置和问题排查。

## MVP 核心闭环

```text
创建优惠券模板 -> 创建发券活动 -> 用户领券 -> 查询用户券 -> 订单试算 -> 锁券 -> 核销/释放
```

## 功能范围

### 商家侧

- 创建优惠券模板。
- 查询优惠券模板详情和列表。
- 启用、停用、过期优惠券模板。
- 创建发券活动。
- 管理活动状态。

### 用户侧

- 用户领取活动优惠券。
- 查询用户已领取优惠券。
- 防止同一用户超过活动领取限制。
- 使用 `requestId` 实现领券请求幂等。

### 结算侧

- 根据订单金额和商品信息筛选可用券。
- 计算每张券的优惠金额。
- 返回当前订单的推荐优惠券。
- 下单时锁定优惠券。
- 支付成功后核销，支付取消或失败后释放。

## 非目标范围

第一阶段不实现以下能力：

- 分库分表。
- 完整用户登录体系，当前仅提供请求头身份上下文和可开关鉴权。
- 复杂营销规则引擎。
- 高并发压测和监控大盘。

## 接口草案

### 系统接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/system/health` | 健康检查 |

### 优惠券模板

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/admin/coupon-templates` | 创建优惠券模板 |
| `GET` | `/api/admin/coupon-templates/{templateId}` | 查询模板详情 |
| `GET` | `/api/admin/coupon-templates` | 查询模板列表 |
| `PATCH` | `/api/admin/coupon-templates/{templateId}/status` | 变更模板状态 |

### 发券活动

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/admin/coupon-campaigns` | 创建发券活动 |
| `GET` | `/api/admin/coupon-campaigns/{campaignId}` | 查询活动详情 |
| `GET` | `/api/admin/coupon-campaigns` | 查询活动列表 |
| `PATCH` | `/api/admin/coupon-campaigns/{campaignId}/status` | 变更活动状态 |

### 用户优惠券

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/user/coupons/receive` | 用户领取优惠券 |
| `GET` | `/api/user/coupons` | 查询用户优惠券 |

### 订单结算

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/settlement/calculate` | 试算订单优惠 |
| `POST` | `/api/settlement/lock` | 锁定优惠券 |
| `POST` | `/api/settlement/confirm` | 确认核销优惠券 |
| `POST` | `/api/settlement/cancel` | 取消订单并释放优惠券 |

## 约束说明

- 所有接口统一返回 `ApiResponse`。
- 领券接口通过 `requestId` 做请求幂等。
- 学习阶段通过请求头传递 `X-User-Id` 和 `X-Admin-Token`，暂不实现完整登录鉴权。
- 金额字段统一使用分为单位的整数，避免浮点精度问题。
