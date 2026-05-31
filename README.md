# XinCoupon

XinCoupon 是一个个人学习性质的优惠券系统 MVP 项目，用于实践 Spring Boot、Redis、事件驱动和典型电商优惠券业务设计。

项目当前采用单体 Spring Boot 工程起步，已经完成“模板创建 -> 活动创建 -> 用户领券 -> 查询用户券 -> 订单试算 -> 锁券 -> 核销/释放”的核心闭环，并补充了 Redis 库存扣减、请求幂等和本地事件消费幂等。

## 免责声明

本项目基于常见电商优惠券业务场景独立设计和实现，仅用于学习、技术交流和个人作品展示。

仓库不包含任何第三方付费课程、商业项目或非公开项目的源码、文档、素材、配置、测试数据和专有实现细节。

## 技术栈

- Java 17
- Spring Boot 3.3.x
- Maven
- Redis
- JUnit 5

后续可以继续增强：

- MySQL
- RocketMQ
- springdoc-openapi 或 Knife4j

## 当前功能

- 优惠券模板管理
- 发券活动管理
- 用户领券和用户券查询
- Redis 活动库存缓存和 Lua 原子扣减
- 领券 `requestId` 幂等控制
- 订单优惠试算、锁券、核销和取消释放
- 领券事件发布和消费幂等处理
- 健康检查接口：`GET /api/system/health`

## 环境要求

- JDK 17+
- Maven 3.8+

## 启动方式

```powershell
mvn spring-boot:run
```

如需连接外部 Redis，可以通过环境变量或 JVM 参数覆盖默认值：

```powershell
mvn spring-boot:run `
  "-Dspring-boot.run.jvmArguments=-DREDIS_HOST=localhost -DREDIS_PORT=6379 -DCACHE_REDIS_PREFIX=xin-coupon:"
```

不要把真实密码写入仓库。生产或共享环境建议使用环境变量、启动参数或本地未提交配置管理。

启动后访问：

```text
GET http://localhost:8080/api/system/health
```

## 项目结构

```text
docs
├── api.md
├── database-design.md
├── requirements.md
└── sql
src/main/java/com/xinjia/coupon
├── admin        # 模板和活动管理
├── common       # 通用响应、异常、枚举、配置
├── dispatch     # 优惠券事件模型、发布和消费幂等
├── settlement   # 试算、锁券、核销、释放
├── system       # 健康检查
└── user         # 领券、用户券查询、库存和幂等
```

## 接口文档

核心接口见 [docs/api.md](docs/api.md)。

## 已完成路线

1. 完成优惠券模板管理。
2. 完成发券活动管理。
3. 完成用户领券和用户券查询。
4. 接入 Redis 实现库存扣减和防超卖。
5. 完成订单优惠试算、锁券、核销和释放。
6. 引入本地事件模拟 MQ 消息流转和消费幂等。

## 后续计划

- 将内存仓储替换为 MySQL 持久化实现。
- 将 Spring 本地事件替换为 RocketMQ 生产者和消费者。
- 补充 OpenAPI 文档和接口示例集合。
- 增加 Docker Compose 本地依赖编排。

## 常用命令

```powershell
mvn test
mvn spring-boot:run
```

## 测试说明

当前测试覆盖模板、活动、领券、Redis 库存扣减、结算、锁券、核销、释放和事件消费幂等。

```powershell
mvn test
```
