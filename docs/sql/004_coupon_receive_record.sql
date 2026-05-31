create table if not exists coupon_receive_record (
    id bigint primary key,
    request_id varchar(64) not null,
    user_id bigint not null,
    campaign_id bigint not null,
    template_id bigint not null,
    user_coupon_id bigint,
    result varchar(32) not null,
    failure_reason varchar(200),
    created_at datetime not null,
    unique key uk_receive_request (request_id),
    index idx_receive_user_campaign (user_id, campaign_id)
) engine = InnoDB
  default charset = utf8mb4
  comment = '领券记录表';
