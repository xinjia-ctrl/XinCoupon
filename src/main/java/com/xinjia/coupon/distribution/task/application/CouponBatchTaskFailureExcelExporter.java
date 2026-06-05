package com.xinjia.coupon.distribution.task.application;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.excel.EasyExcel;
import com.xinjia.coupon.distribution.task.domain.CouponBatchTaskFailure;
import com.xinjia.coupon.distribution.task.web.CouponBatchTaskFailureExcelRow;

@Component
public class CouponBatchTaskFailureExcelExporter {

    public byte[] export(List<CouponBatchTaskFailure> failures) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        List<CouponBatchTaskFailureExcelRow> rows = failures.stream()
                .map(CouponBatchTaskFailureExcelRow::from)
                .toList();
        EasyExcel.write(outputStream, CouponBatchTaskFailureExcelRow.class)
                .sheet("失败记录")
                .doWrite(rows);
        return outputStream.toByteArray();
    }
}
