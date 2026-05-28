create table coupon_campaign (
    id bigint primary key,
    template_id bigint not null,
    merchant_id bigint not null,
    name varchar(80) not null,
    campaign_stock int not null,
    received_count int not null default 0,
    per_user_limit int not null,
    start_time datetime not null,
    end_time datetime not null,
    status varchar(32) not null,
    created_at datetime not null,
    updated_at datetime not null,
    index idx_campaign_template (template_id),
    index idx_campaign_status_time (status, start_time, end_time)
);
