package com.xinjia.coupon.distribution.task.application;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.alibaba.excel.EasyExcel;
import com.xinjia.coupon.distribution.task.web.CouponBatchTaskExcelRow;

@Component
public class CouponBatchTaskExcelReader {

    public List<Long> readUserIds(InputStream inputStream) {
        List<CouponBatchTaskExcelRow> rows = EasyExcel.read(inputStream)
                .head(CouponBatchTaskExcelRow.class)
                .sheet()
                .doReadSync();
        return rows.stream()
                .map(CouponBatchTaskExcelRow::getUserId)
                .filter(Objects::nonNull)
                .toList();
    }
}
