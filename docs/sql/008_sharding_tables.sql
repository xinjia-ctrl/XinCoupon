-- 可选分表脚本：先执行 003_user_coupon.sql 和 007_coupon_batch_task.sql，再执行本脚本。
-- 当前项目默认仍访问原逻辑表，分表脚本用于后续接入 ShardingSphere 或分片仓储。

create table if not exists user_coupon_0 like user_coupon;
create table if not exists user_coupon_1 like user_coupon;

create table if not exists coupon_batch_task_0 like coupon_batch_task;
create table if not exists coupon_batch_task_1 like coupon_batch_task;
