create table if not exists coupon_settlement (
    id bigint primary key,
    user_id bigint not null,
    user_coupon_id bigint not null,
    order_no varchar(64) not null,
    status varchar(32) not null,
    locked_at datetime,
    paid_at datetime,
    canceled_at datetime,
    refunded_at datetime,
    created_at datetime not null,
    updated_at datetime not null,
    unique key uk_order_coupon (order_no, user_coupon_id),
    index idx_settlement_coupon_status (user_id, user_coupon_id, status),
    index idx_settlement_order (order_no)
) engine = InnoDB
  default charset = utf8mb4
  comment = '优惠券结算单表';
