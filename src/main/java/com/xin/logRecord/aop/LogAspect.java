package com.xin.logRecord.aop;

import com.xin.logRecord.annotation.LogRecord;
import com.xin.logRecord.beans.LogRecordDTO;
import com.xin.logRecord.beans.LogRecordPostProcessor;
import com.xin.logRecord.context.LogRecordContext;
import com.xin.logRecord.parse.LogRecordExpressionParser;
import com.xin.logRecord.service.LogRecordCustomService;
import com.xin.logRecord.service.impl.DefaultLogRecordService;
import com.xin.logRecord.thread.LogRecordThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Slf4j
@Aspect
@Component
public class LogAspect {
    /**
     * LogRecord后置处理器
     */
    @Autowired(required = false)
    private LogRecordPostProcessor logRecordPostProcessor;

    @Autowired
    private LogRecordThreadPool logRecordThreadPool;

    @Autowired(required = false)
    private LogRecordCustomService logRecordCustomService;

    @Autowired
    private DefaultLogRecordService defaultLogRecordService;

    @Around("@annotation(com.xin.logRecord.annotation.LogRecord) || @annotation(com.xin.logRecord.annotation.LogRecords)")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        Object result = null;
        Method method = getMethod(point);
        // 获取方法上的所有注解
        LogRecord[] annotations = method.getAnnotationsByType(LogRecord.class);
        // 将前置和后置执行分开处理并保证顺序
        Map<LogRecord, LogRecordDTO> logDTOMap = new LinkedHashMap<>();

        StopWatch stopWatch = new StopWatch();
        try {
            // 方法执行前进行SpEL解析和logDTOMap装配
            for (LogRecord annotation : annotations) {
                if (annotation.executeBeforeFunc()) {
                    LogRecordDTO logRecordDTO = LogRecordExpressionParser.parseExpress(annotation, point, getMethod(point));
                    if (logRecordDTO != null) {
                        logDTOMap.put(annotation, logRecordDTO);
                    }
                }
            }

            // 方法执行时间计时
            stopWatch.start();
            result = point.proceed();
            stopWatch.stop();

            // 方法执行后进行SpEL解析和logDTOMap装配
            for (LogRecord annotation : annotations) {
                if (!annotation.executeBeforeFunc()) {
                    LogRecordDTO logRecordDTO = LogRecordExpressionParser.parseExpress(annotation, point, getMethod(point));
                    if (logRecordDTO != null) {
                        logDTOMap.put(annotation, logRecordDTO);
                    }
                }
            }
        } catch (Throwable throwable) {
            stopWatch.stop();
            // 方法执行有异常，写入异常信息并采用失败的文本模板
            logDTOMap.values().forEach(logRecordDTO -> {
                logRecordDTO.setSuccess(false);
                logRecordDTO.setException(throwable.getMessage());
            });
            throw throwable;
        } finally {
            // logDtoMap最终装配
            logDTOMap.forEach((logRecord, logRecordDTO) -> {
                logRecordDTO.setExecutionTime(stopWatch.getTotalTimeMillis());
                LogRecordExpressionParser.parseExpressTemplate(logRecord, logRecordDTO);
            });
            // 扩展点 postProcessAfterInitialization
            if (logRecordPostProcessor != null) {
                logDTOMap.forEach((logRecord, logRecordDTO) -> {
                    try {
                        logRecordPostProcessor.postProcessAfterInitialization(logRecordDTO);
                    } catch (Exception e) {
                        log.error("LogAspect logRecordPostProcessor error", e);
                    }
                });
            }
            // 线程池分发
            ExecutorService executorService = logRecordThreadPool.getLogReocrdThreadPoolExecutorService();
            logDTOMap.forEach((logRecord, logRecordDTO) -> {
                executorService.submit(() -> {
                    // 扩展点，自定义日志记录监听
                    if (logRecordCustomService != null) {
                        logRecordCustomService.record(logRecordDTO);
                    }else {
                        // 默认日志记录实现
                        defaultLogRecordService.record(logRecordDTO);
                    }
                });
            });

            // 清除上下文变量
            LogRecordContext.clearContext();
        }
        return result;
    }

    // 根据joinPoint获取其方法
    public Method getMethod(JoinPoint joinPoint) {
        Method method = null;
        try {
            Signature signature = joinPoint.getSignature();
            MethodSignature ms = (MethodSignature) signature;
            Object target = joinPoint.getTarget();
            method = target.getClass().getMethod(ms.getName(), ms.getParameterTypes());
        } catch (NoSuchMethodException e) {
            log.error("OperationLogAspect getMethod error", e);
        }
        return method;
    }
}
