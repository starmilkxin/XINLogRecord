package com.xin.logRecord.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "log-record")
public class LogRecordProperties {
    private int poolSize = 4;
}
