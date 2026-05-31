create table if not exists user_coupon (
    id bigint primary key,
    user_id bigint not null,
    template_id bigint not null,
    campaign_id bigint not null,
    coupon_code varchar(64) not null,
    status varchar(32) not null,
    received_at datetime not null,
    locked_at datetime,
    used_at datetime,
    expired_at datetime not null,
    order_no varchar(64),
    created_at datetime not null,
    updated_at datetime not null,
    unique key uk_coupon_code (coupon_code),
    index idx_user_coupon_status (user_id, status),
    index idx_user_campaign (user_id, campaign_id)
) engine = InnoDB
  default charset = utf8mb4
  comment = '用户优惠券表';
