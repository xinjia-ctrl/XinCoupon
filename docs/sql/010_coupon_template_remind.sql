create table if not exists coupon_template_remind (
    id bigint primary key,
    user_id bigint not null,
    template_id bigint not null,
    remind_type varchar(32) not null,
    remind_at datetime not null,
    status varchar(32) not null,
    created_at datetime not null,
    updated_at datetime not null,
    unique key uk_user_template_active (user_id, template_id, status),
    index idx_remind_user_status (user_id, status),
    index idx_remind_time (remind_at)
) engine = InnoDB
  default charset = utf8mb4
  comment = '优惠券模板预约提醒表';
