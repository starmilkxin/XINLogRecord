package com.xin.logRecord.parse;

import com.xin.logRecord.annotation.LogRecord;
import com.xin.logRecord.beans.LogRecordDTO;
import com.xin.logRecord.constant.PrefixConstant;
import com.xin.logRecord.context.LogRecordContext;
import com.xin.logRecord.function.LogRecordFunctionFactory;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class LogRecordExpressionParser {
    /**
     * 通过Spring提供的ParameterNameDiscoverer接口可以通过asm获取了class文件的LocalVariableTable信息，
     * LocalVariableTable里不仅保存了参数名，还保存了其他局部变量信息
     */
    private static final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    private static final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 解析注解中的SpEL表达式，填充logRecordDTO
     */
    public static LogRecordDTO parseExpress(LogRecord logRecord, JoinPoint joinPoint, Method method) {
        LogRecordDTO logRecordDTO = null;
        Date operateDate = new Date();
        String bizId = logRecord.bizId();
        String bizType = logRecord.bizType();
        String operatorId = logRecord.operatorId();
        String extra = logRecord.extra();
        try {
            Object[] args = joinPoint.getArgs();
            String[] params = discoverer.getParameterNames(method);
            if (params != null) {
                for (int len = 0; len < params.length; len++) {
                    // 参数内容存储在context中
                    LogRecordContext.putVariable(params[len], args[len]);
                }
            }
            // 用于解析SpEL表达式的自定义容器
            StandardEvaluationContext standardEvaluationContext = LogRecordContext.getContext();
            // 注册所有自定义函数
            LogRecordFunctionFactory.registerFunction(standardEvaluationContext);
            // condition 处理：SpEL解析
            String condition = logRecord.condition();
            if (StringUtils.isNotBlank(condition)) {
                Expression conditionExpression = parser.parseExpression(condition);
                condition = conditionExpression.getValue(standardEvaluationContext, String.class);
                // 条件不满足直接返回null
                if (condition == null || !condition.equals("true")) {
                    return logRecordDTO;
                }
            }
            // bizId 处理：SpEL解析
            if (StringUtils.isNotBlank(bizId)) {
                Expression bizIdExpression = parser.parseExpression(bizId);
                bizId = bizIdExpression.getValue(standardEvaluationContext, String.class);
            }
            // bizType 处理：SpEL解析
            if (StringUtils.isNotBlank(bizType)) {
                Expression bizTypeExpression = parser.parseExpression(bizType);
                bizType = bizTypeExpression.getValue(standardEvaluationContext, String.class);
            }
            // operatorId 处理：SpEL解析
            if (StringUtils.isNotBlank(operatorId)) {
                Expression operatorIdExpression = parser.parseExpression(operatorId);
                operatorId = operatorIdExpression.getValue(standardEvaluationContext, String.class);
            }
            // extra 处理：SpEL解析
            if (StringUtils.isNotBlank(extra)) {
                Expression extraExpression = parser.parseExpression(extra);
                extra = extraExpression.getValue(standardEvaluationContext, String.class);
            }
        } catch (Exception e) {
            log.error("LogAspect parseExpress error", e);
        } finally {
            logRecordDTO = LogRecordDTO.builder()
                    .logId(UUID.randomUUID().toString())
                    .operateDate(operateDate)
                    .bizId(bizId)
                    .bizType(bizType)
                    .operatorId(operatorId)
                    .success(true)
                    .extra(extra)
                    .build();
        }
        return logRecordDTO;
    }

    /**
     * 根据方法是否执行成功，解析对应的模板，填充logRecordDTO的模板信息和最终msg
     */
    public static void parseExpressTemplate(LogRecord logRecord, LogRecordDTO logRecordDTO) {
        String templateMsg = null;
        StringBuilder msg = new StringBuilder("");
        try {
            // 用于解析SpEL表达式的自定义容器
            StandardEvaluationContext standardEvaluationContext = LogRecordContext.getContext();
            // 注册所有自定义函数
            LogRecordFunctionFactory.registerFunction(standardEvaluationContext);
            // templateMsg 处理：SpEL解析
            if (logRecordDTO.getSuccess()) {
                if (StringUtils.isNotBlank(logRecord.success())) {
                    Expression successExpression = parser.parseExpression(logRecord.success());
                    templateMsg = successExpression.getValue(standardEvaluationContext, String.class);
                }
            }else {
                if (StringUtils.isNotBlank(logRecord.fail())) {
                    Expression failExpression = parser.parseExpression(logRecord.fail());
                    templateMsg = failExpression.getValue(standardEvaluationContext, String.class);
                }
            }
        } catch (Exception e) {
            log.error("LogAspect parseExpressTemplate error", e);
        } finally {
            logRecordDTO.setTemplateMsg(templateMsg);
            // 解析prefix和suffix
            String prefixAndSuffix = PrefixConstant.getPrefixAndSuffix(logRecord.prefix());
            String prefix = (prefixAndSuffix == null || prefixAndSuffix.length() == 0) ? "" : prefixAndSuffix.substring(0,1);
            String suffix = (prefixAndSuffix == null || prefixAndSuffix.length() == 0) ? "" : prefixAndSuffix.substring(1,2);
            // 反射获取logRecordDTO中的所有信息，构成msg
            Field[] fields = logRecordDTO.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                // 不包括msg本身
                if (field.getName().equals("msg")) {
                    continue;
                }
                try {
                    msg.append(", ")
                            .append(field.getName())
                            .append("=")
                            .append(field.get(logRecordDTO) == null ? "" : field.get(logRecordDTO).toString());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            msg.delete(0, 2);
            msg.insert(0, prefix);
            msg.append(suffix);
            logRecordDTO.setMsg(msg.toString());
        }
    }
}
