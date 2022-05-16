package com.xin.logRecord.thread;

import com.xin.logRecord.configuration.LogRecordProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Slf4j
@Component
public class LogRecordThreadPool {
    private static final ThreadFactory THREAD_FACTORY = new CustomizableThreadFactory("log-record-");

    private final ExecutorService LOG_RECORD_THREAD_POOL_EXECUTOR_SERVICE;

    public LogRecordThreadPool(LogRecordProperties logRecordProperties) {
        log.info("LOG_RECORD_THREAD_POOL_EXECUTOR init poolSize [{}]", logRecordProperties.getPoolSize());
        this.LOG_RECORD_THREAD_POOL_EXECUTOR_SERVICE = new ThreadPoolExecutor(logRecordProperties.getPoolSize(), logRecordProperties.getPoolSize(), 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1024), THREAD_FACTORY, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public ExecutorService getLogReocrdThreadPoolExecutorService() {
        return this.LOG_RECORD_THREAD_POOL_EXECUTOR_SERVICE;
    }
}
