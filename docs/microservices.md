# 微服务模块拆分说明

XinCoupon 当前已拆为 Maven 多模块工程，整体形态对齐 onecoupon 的服务边界。现阶段采用 `coupon-common` 承载已有领域代码和基础设施，各服务模块提供独立启动入口、独立应用名和独立端口，后续可继续把服务间直接调用替换为 Feign 或 RocketMQ。

## 模块职责

| 模块 | 默认端口 | 职责 |
| --- | ---: | --- |
| `coupon-gateway` | `9000` | 网关入口、统一鉴权和路由扩展入口 |
| `coupon-merchant-admin` | `9001` | 商家端模板、活动、批量任务管理 |
| `coupon-engine` | `9002` | 用户领券、秒杀扣减、提醒和用户券状态 |
| `coupon-distribution` | `9003` | EasyExcel 批量发券、Redis 暂存、失败导出 |
| `coupon-settlement` | `9004` | 结算试算、锁券、核销、取消、退款 |
| `coupon-search` | `9005` | ES 搜索、Canal 同步、索引重建和补偿 |
| `coupon-common` | 无 | 共享领域模型、基础设施、配置、测试兼容模块 |

## 常用命令

全量测试：

```powershell
mvn test
```

全量打包：

```powershell
mvn package -DskipTests
```

启动指定服务：

```powershell
mvn -pl coupon-engine spring-boot:run
mvn -pl coupon-distribution spring-boot:run
mvn -pl coupon-search spring-boot:run
```

服务端口可以通过环境变量覆盖，例如：

```powershell
$env:ENGINE_SERVER_PORT="9102"
mvn -pl coupon-engine spring-boot:run
```

## 后续演进

1. 将 `coupon-common` 中的业务包逐步迁移到对应服务模块。
2. 将跨服务直接调用改为 OpenFeign 或 RocketMQ 事件。
3. 将共享数据库表按服务职责拆分为独立 schema。
4. 为 `coupon-gateway` 接入 Spring Cloud Gateway 路由配置。
