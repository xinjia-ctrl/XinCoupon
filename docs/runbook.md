# 本地运行与联调手册

## 1. 准备依赖

本项目默认使用 `dev` profile，本地联调至少需要 MySQL。Redis、RocketMQ、Nacos 可按需开启。

| 依赖 | 默认配置 | 说明 |
| --- | --- | --- |
| MySQL | `192.168.100.128:3306` | 数据库名 `xin_coupon`，用户名默认 `root` |
| Redis | `localhost:6379` | 用于活动库存缓存和防超卖 |
| RocketMQ | 默认关闭 | 配置 `rocketmq.name-server` 后启用消息发送 |
| Nacos | 默认关闭 | 配置 `NACOS_DISCOVERY_ENABLED=true` 后注册服务 |

真实密码不要写入仓库，使用环境变量或 JVM 参数注入。

## 2. 初始化数据库

按文件名顺序执行 `docs/sql` 下的 SQL：

```text
docs/sql/000_create_database.sql
docs/sql/001_coupon_template.sql
docs/sql/002_coupon_campaign.sql
docs/sql/003_user_coupon.sql
docs/sql/004_coupon_receive_record.sql
docs/sql/005_coupon_event_log.sql
docs/sql/006_coupon_operation_log.sql
```

## 3. 启动应用

只连接 MySQL：

```powershell
mvn spring-boot:run `
  "-Dspring-boot.run.jvmArguments=-DMYSQL_HOST=192.168.100.128 -DMYSQL_PORT=3306 -DMYSQL_DATABASE=xin_coupon -DMYSQL_USERNAME=root -DMYSQL_PASSWORD=<MySQL密码>"
```

启用 Redis、RocketMQ、Nacos 和请求头鉴权：

```powershell
mvn spring-boot:run `
  "-Dspring-boot.run.jvmArguments=-DMYSQL_HOST=192.168.100.128 -DMYSQL_PASSWORD=<MySQL密码> -Dunique-name=ljx123 -Dframework.cache.redis.prefix=ljx123: -Dspring.data.redis.host=<Redis地址> -Dspring.data.redis.port=<Redis端口> -Dspring.data.redis.password=<Redis密码> -Drocketmq.enabled=true -Drocketmq.name-server=<RocketMQ地址> -Drocketmq.producer.group=xin-coupon-producer -Dspring.cloud.nacos.discovery.enabled=true -Dspring.cloud.nacos.discovery.server-addr=<Nacos地址> -DAUTH_ENABLED=true -DAUTH_ADMIN_TOKEN=<管理端令牌>"
```

## 4. 验证入口

```text
GET http://localhost:8080/api/system/health
GET http://localhost:8080/doc.html
GET http://localhost:8080/v3/api-docs
```

开启鉴权后：

- 管理端接口携带 `X-Admin-Token`。
- 用户券和结算接口携带 `X-User-Id`。

## 5. 核心联调顺序

```text
创建模板 -> 启用模板 -> 创建活动 -> 启动活动 -> 用户领券 -> 查询用户券 -> 订单试算 -> 锁券 -> 核销或释放
```

详细请求示例见 `docs/api.md`。

## 6. 发布前验证

```powershell
mvn test
```

确认测试通过后，再检查是否误提交敏感信息：

```powershell
rg "真实密码|明文密码|私有令牌" README.md docs src/main/resources
```
