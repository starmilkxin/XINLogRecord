package com.xin.logRecord.beans;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class LogRecordDTO {
    // 日志id
    private String logId;

    // 操作时间
    private Date operateDate;

    // 操作日志绑定的业务对象标识
    private String bizId;

    // 操作日志绑定的业务对象类别
    private String bizType;

    // 操作日志的执行人Id
    private String operatorId;

    // 是否成功
    private Boolean success;

    // 模板信息
    private String templateMsg;

    // 异常信息
    private String exception;

    // 额外信息
    private String extra;

    // 方法执行时长(毫秒)
    private Long executionTime;

    // 日志全部信息
    private String msg;
}
