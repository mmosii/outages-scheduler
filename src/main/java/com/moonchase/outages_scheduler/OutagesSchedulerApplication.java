package com.moonchase.outages_scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OutagesSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutagesSchedulerApplication.class, args);
    }
}
