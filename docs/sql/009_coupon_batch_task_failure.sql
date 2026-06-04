create table if not exists coupon_batch_task_failure (
    id bigint primary key,
    task_id bigint not null,
    batch_no varchar(64) not null,
    user_id bigint not null,
    row_number int not null,
    failure_reason varchar(255) not null,
    created_at datetime not null,
    index idx_task_failure_task (task_id),
    index idx_task_failure_batch (batch_no)
) engine = InnoDB
  default charset = utf8mb4
  comment = '批量发券任务失败明细表';
