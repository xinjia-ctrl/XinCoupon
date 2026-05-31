create table if not exists coupon_event_log (
    id bigint primary key,
    event_id varchar(64) not null,
    event_type varchar(64) not null,
    biz_id varchar(64) not null,
    payload text,
    consume_status varchar(32) not null,
    created_at datetime not null,
    updated_at datetime not null,
    unique key uk_event_id (event_id),
    index idx_event_type_status (event_type, consume_status)
) engine = InnoDB
  default charset = utf8mb4
  comment = '优惠券事件日志表';
