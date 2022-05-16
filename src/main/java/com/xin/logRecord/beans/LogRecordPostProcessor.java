package com.xin.logRecord.beans;

import org.springframework.lang.Nullable;

public interface LogRecordPostProcessor {
    @Nullable
    void postProcessAfterInitialization(LogRecordDTO logRecordDTO) throws Exception;
}
