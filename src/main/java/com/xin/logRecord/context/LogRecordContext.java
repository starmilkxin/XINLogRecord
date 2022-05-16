package com.xin.logRecord.context;

import org.springframework.expression.spel.support.StandardEvaluationContext;

// 记录操作日志上下文信息
public class LogRecordContext {
    private static final ThreadLocal<StandardEvaluationContext> logRecordContext = new ThreadLocal<>();

    public static StandardEvaluationContext getContext() {
        StandardEvaluationContext context = null;
        if ((context = logRecordContext.get()) == null) {
            logRecordContext.set(context = new StandardEvaluationContext());
        }
        return context;
    }

    public static void putVariable(String name, Object value) {
        StandardEvaluationContext context = LogRecordContext.getContext();
        context.setVariable(name, value);
    }

    public static void clearContext() {
        logRecordContext.remove();
    }
}
