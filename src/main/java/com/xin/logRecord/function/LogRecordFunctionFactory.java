package com.xin.logRecord.function;

import org.springframework.expression.spel.support.StandardEvaluationContext;

public class LogRecordFunctionFactory {

    public static void registerFunction(StandardEvaluationContext context) {
        LogRecordFunctionRegister.registerFunction(context);
    }
}
