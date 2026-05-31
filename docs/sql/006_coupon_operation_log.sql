create table if not exists coupon_operation_log (
    id bigint primary key,
    biz_type varchar(64) not null,
    biz_id varchar(64) not null,
    operation_type varchar(64) not null,
    operator_id bigint,
    operation_detail varchar(500),
    created_at datetime not null,
    index idx_operation_biz (biz_type, biz_id),
    index idx_operation_type_time (operation_type, created_at)
) engine = InnoDB
  default charset = utf8mb4
  comment = '优惠券操作日志表';
