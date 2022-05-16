package com.xin.logRecord.service.impl;

import com.xin.logRecord.beans.LogRecordDTO;
import com.xin.logRecord.service.LogRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DefaultLogRecordService implements LogRecordService {
    @Override
    public void record(LogRecordDTO logRecordDTO) {
        log.info("{}", logRecordDTO.getMsg());
    }
}
