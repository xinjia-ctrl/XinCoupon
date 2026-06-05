# 领券链路 QPS 压测报告

## 压测目标

验证高并发抢券链路在 Redis Lua 原子扣减、用户领取次数上限校验、数据库库存兜底和请求幂等共同作用下的吞吐能力。

目标口径：

- 单机领券接口峰值 QPS：`3000+`
- 成功率：不低于 `99%`
- 不出现库存超发
- 重复请求不会重复发券

## 压测脚本

脚本位置：

```powershell
scripts\benchmark-receive.ps1
```

示例命令：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\benchmark-receive.ps1 `
  -BaseUrl http://localhost:8080 `
  -CampaignId 2001 `
  -TotalRequests 10000 `
  -Concurrency 300 `
  -UserIdStart 100000
```

脚本会输出：

- `docs/performance/results/<timestamp>/summary.json`
- `docs/performance/results/<timestamp>/detail.csv`

## 前置条件

1. MySQL、Redis 已启动。
2. 已创建并启用优惠券模板和活动。
3. 活动库存大于 `TotalRequests`。
4. 活动 `perUserLimit` 不小于 `1`。
5. Redis 中活动库存已预热。
6. 本机或压测机与服务端网络稳定。

## 指标口径

| 指标 | 说明 |
| --- | --- |
| `qps` | 总请求数 / 脚本整体耗时 |
| `successRate` | HTTP `2xx` 请求占比 |
| `p50Ms` | 请求耗时第 50 分位 |
| `p95Ms` | 请求耗时第 95 分位 |
| `p99Ms` | 请求耗时第 99 分位 |
| `failureCount` | 非 `2xx` 或请求异常数量 |

## 推荐压测档位

| 档位 | TotalRequests | Concurrency | 用途 |
| --- | ---: | ---: | --- |
| smoke | 1000 | 50 | 验证环境和数据准备 |
| baseline | 10000 | 200 | 获取基础吞吐 |
| target | 30000 | 300-500 | 验证 `3000+ QPS` |
| soak | 100000 | 300 | 观察稳定性和库存一致性 |

## 结果记录

当前仓库提供压测脚本和报告模板，实际 QPS 需要在目标机器执行脚本后填入。

| 时间 | 环境 | TotalRequests | Concurrency | QPS | 成功率 | P95 | P99 | 结论 |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 待执行 | 本机/测试环境 | 30000 | 300-500 | 待填 | 待填 | 待填 | 待填 | 待填 |

## 结果校验

压测结束后建议执行以下检查：

1. 活动表 `received_count + available_stock` 与初始库存一致。
2. `user_coupon` 中同一 `campaign_id + user_id` 不超过活动限领次数。
3. `coupon_receive_record` 中同一 `request_id` 只对应一个结果。
4. Redis 活动库存与数据库库存没有出现负数。

## 结论模板

在 `target` 档位下，如果 `summary.json` 中 `qps >= 3000`，且库存与领取记录校验通过，可以写为：

> 单机抢券压测 QPS 达到 3000+，未出现库存超发和重复发券。
