# 分片改造说明

## 目标

当前项目先保留单体可运行模式，新增分片路由和分表脚本，为后续接入 ShardingSphere 做准备。

本阶段不默认启用分库分表，避免本地启动依赖额外数据源配置；应用启动、测试和核心接口仍然可以按原方式运行。

## 分片规则

| 逻辑表 | 分片键 | 默认分表数 | 路由规则 |
| --- | --- | --- | --- |
| `user_coupon` | `user_id` | 2 | `user_id % tableCount` |
| `coupon_batch_task` | `batch_no` | 2 | `hash(batch_no) % tableCount` |

代码入口：

- `CouponShardRouter`：分片路由接口
- `ModuloCouponShardRouter`：取模路由实现
- `ShardingProperties`：分表数量配置

配置项：

```yaml
xincoupon:
  sharding:
    enabled: false
    user-coupon-table-count: 2
    coupon-batch-task-table-count: 2
```

## MySQL 分表脚本

先执行原始建表脚本，再执行：

```powershell
mysql -h 192.168.100.128 -P 3306 -u root -p xin_coupon < docs\sql\008_sharding_tables.sql
```

脚本会创建：

- `user_coupon_0`
- `user_coupon_1`
- `coupon_batch_task_0`
- `coupon_batch_task_1`

## 后续接 ShardingSphere 的落点

后续可以把 `ModuloCouponShardRouter` 中的规则平移到 ShardingSphere-JDBC：

- `user_coupon` 使用 `user_id` 作为标准分片键。
- `coupon_batch_task` 使用 `batch_no` 作为标准分片键。
- 默认保留原逻辑表名，由 ShardingSphere 根据分片规则改写到实际表。

这样可以在不改变接口层和业务层请求模型的前提下，把单体持久化逐步切到分表模式。
