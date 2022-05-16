package com.xin.logRecord.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LogRecordProperties.class)
@ComponentScan("com.xin.logRecord")
public class LogRecordAutoConfiguration {
}
