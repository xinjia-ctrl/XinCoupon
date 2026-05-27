# XinCoupon

XinCoupon 是一个个人学习性质的优惠券系统 MVP 项目，用于实践 Spring Boot、Redis、消息队列和典型电商优惠券业务设计。

项目当前采用单体 Spring Boot 工程起步，先完成“模板创建 -> 活动创建 -> 用户领券 -> 查询用户券 -> 订单试算 -> 锁券 -> 核销/释放”的核心闭环，再逐步补充 Redis 防超卖、消息事件和接口文档。

## 免责声明

本项目基于常见电商优惠券业务场景独立设计和实现，仅用于学习、技术交流和个人作品展示。

仓库不包含任何第三方付费课程、商业项目或非公开项目的源码、文档、素材、配置、测试数据和专有实现细节。

## 技术栈

- Java 17
- Spring Boot 3.3.x
- Maven
- JUnit 5

后续计划按 MVP 进度逐步引入：

- MySQL
- Redis
- RabbitMQ、RocketMQ 或 Kafka 三选一
- springdoc-openapi 或 Knife4j

## 当前功能

- Spring Boot 单体工程骨架
- 基础运行配置
- 健康检查接口：`GET /api/system/health`
- 预留业务域包：`admin`、`user`、`settlement`、`dispatch`

## 环境要求

- JDK 17+
- Maven 3.8+

## 启动方式

```powershell
mvn spring-boot:run
```

启动后访问：

```text
GET http://localhost:8080/api/system/health
```

## 项目结构

```text
src
├── main
│   ├── java
│   │   └── com.xinjia.coupon
│   │       ├── admin
│   │       ├── dispatch
│   │       ├── settlement
│   │       ├── system
│   │       ├── user
│   │       └── XinCouponApplication.java
│   └── resources
│       ├── application.yml
│       └── application-dev.yml
└── test
    └── java
        └── com.xinjia.coupon
```

## 开发路线

1. 完成优惠券模板管理。
2. 完成发券活动管理。
3. 完成用户领券和用户券查询。
4. 接入 Redis 实现库存扣减和防超卖。
5. 完成订单优惠试算、锁券、核销和释放。
6. 引入消息事件处理异步日志或通知。

## 常用命令

```powershell
mvn test
mvn spring-boot:run
```
