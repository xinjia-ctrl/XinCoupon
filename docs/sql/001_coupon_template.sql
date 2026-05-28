create table coupon_template (
    id bigint primary key,
    merchant_id bigint not null,
    title varchar(80) not null,
    coupon_type varchar(32) not null,
    discount_amount bigint,
    discount_rate int,
    threshold_amount bigint not null default 0,
    valid_start_time datetime not null,
    valid_end_time datetime not null,
    total_stock int not null,
    status varchar(32) not null,
    created_at datetime not null,
    updated_at datetime not null,
    index idx_template_merchant_status (merchant_id, status),
    index idx_template_valid_time (valid_start_time, valid_end_time)
);
