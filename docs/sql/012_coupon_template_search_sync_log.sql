create table if not exists coupon_template_search_sync_log (
    id bigint primary key,
    event_type varchar(64) not null,
    template_id bigint not null,
    payload json not null,
    consumed tinyint not null default 0,
    created_at datetime not null,
    consumed_at datetime,
    index idx_search_sync_consumed (consumed, created_at),
    index idx_search_sync_template (template_id)
) engine = InnoDB
  default charset = utf8mb4
  comment = '优惠券模板搜索同步日志表';
