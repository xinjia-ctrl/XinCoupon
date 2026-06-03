# XinCoupon

XinCoupon 是一个个人学习性质的优惠券系统 MVP 项目，用于实践 Spring Boot、Redis、事件驱动和典型电商优惠券业务设计。

项目当前采用单体 Spring Boot 工程起步，已经完成“模板创建 -> 活动创建 -> 用户领券 -> 查询用户券 -> 订单试算 -> 锁券 -> 核销/释放”的核心闭环，并补充了 Redis 库存扣减、请求幂等、RocketMQ 事件发送与消费幂等、Nacos 服务注册和 Knife4j 接口文档。

## 免责声明

本项目基于常见电商优惠券业务场景独立设计和实现，仅用于学习、技术交流和个人作品展示。

仓库不包含任何第三方付费课程、商业项目或非公开项目的源码、文档、素材、配置、测试数据和专有实现细节。

## 技术栈

- Java 17
- Spring Boot 3.3.x
- Maven
- MySQL
- MyBatis-Plus
- Redis
- RocketMQ
- Spring Cloud Alibaba Nacos
- Knife4j / springdoc-openapi
- JUnit 5

## 当前功能

- 优惠券模板管理
- 发券活动管理
- 用户领券和用户券查询
- Redis 活动库存缓存和 Lua 原子扣减
- 领券 `requestId` 幂等控制
- 订单优惠试算、锁券、核销和取消释放
- 领券事件发布和消费幂等处理
- Nacos 服务注册配置，默认关闭，可通过启动参数开启
- Knife4j 接口文档入口
- 健康检查接口：`GET /api/system/health`

## 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x，开发环境默认连接 `192.168.100.128:3306`
- Redis 6.x+
- RocketMQ 4.x/5.x 兼容服务端，未配置 `rocketmq.name-server` 时默认跳过 MQ Bean 创建
- Nacos 2.x，默认不注册服务

## 启动方式

```powershell
mvn spring-boot:run
```

开发环境默认读取 `application-dev.yml`，MySQL 默认主机为 `192.168.100.128`，数据库名为 `xin_coupon`，用户名为 `root`。密码不要写入仓库，启动时通过环境变量或 JVM 参数传入：

```powershell
mvn spring-boot:run `
  "-Dspring-boot.run.jvmArguments=-DMYSQL_HOST=192.168.100.128 -DMYSQL_PORT=3306 -DMYSQL_DATABASE=xin_coupon -DMYSQL_USERNAME=root -DMYSQL_PASSWORD=<MySQL密码>"
```

如需连接外部 Redis，可以通过环境变量或 JVM 参数覆盖默认值：

```powershell
mvn spring-boot:run `
  "-Dspring-boot.run.jvmArguments=-DREDIS_HOST=localhost -DREDIS_PORT=6379 -DCACHE_REDIS_PREFIX=xin-coupon:"
```

如需同时启用公有云 Redis、RocketMQ 和 Nacos，使用启动参数注入地址和密码。下面只保留占位符，真实密码不要提交到仓库：

```powershell
mvn spring-boot:run `
  "-Dspring-boot.run.jvmArguments=-Dunique-name=ljx123 -Dframework.cache.redis.prefix=ljx123: -Dspring.data.redis.host=<Redis地址> -Dspring.data.redis.port=<Redis端口> -Dspring.data.redis.password=<Redis密码> -Drocketmq.enabled=true -Drocketmq.name-server=<RocketMQ地址> -Drocketmq.producer.group=xin-coupon-producer -Dspring.cloud.nacos.discovery.enabled=true -Dspring.cloud.nacos.discovery.server-addr=<Nacos地址>"
```

不要把真实密码写入仓库。生产或共享环境建议使用环境变量、启动参数或本地未提交配置管理。

启动后访问：

```text
GET http://localhost:8080/api/system/health
```

接口文档入口：

```text
http://localhost:8080/doc.html
http://localhost:8080/v3/api-docs
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

核心接口见 [docs/api.md](docs/api.md)，启动后也可以访问 Knife4j 页面 `http://localhost:8080/doc.html`。

## 已完成路线

1. 完成优惠券模板管理。
2. 完成发券活动管理。
3. 完成用户领券和用户券查询。
4. 接入 Redis 实现库存扣减和防超卖。
5. 完成订单优惠试算、锁券、核销和释放。
6. 引入本地事件模拟 MQ 消息流转和消费幂等。
7. 接入 MyBatis-Plus、RocketMQ、Nacos 和 Knife4j 基础能力。

## 后续计划

- 补充更多接口示例和联调用例。
- 增加 Nacos 配置中心拆分。
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
