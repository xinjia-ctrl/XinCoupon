package com.xinjia.coupon.dispatch.event.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CouponEventLogMapper extends BaseMapper<CouponEventLogDO> {
}
