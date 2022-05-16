package com.xin.logRecord.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(LogRecords.class)
public @interface LogRecord {
    // 操作日志成功的文本模板
    String success();

    // 操作日志失败的文本模板
    String fail() default "";

    // 操作日志绑定的业务对象标识
    String bizId();

    // 操作日志绑定的业务对象类别
    String bizType() default "";

    // 操作日志的执行人Id
    String operatorId() default "";

    // 记录日志的条件
    String condition() default "";

    // 注解中参数的字符拼接选择
    int prefix() default 1;

    // 额外信息
    String extra() default "";

    // 是否在方法执行前对SpEL进行解析
    boolean executeBeforeFunc() default true;
}
