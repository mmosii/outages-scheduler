package com.moonchase.outages_scheduler.config;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAsync(proxyTargetClass = true)
public class AsyncConfig {
}