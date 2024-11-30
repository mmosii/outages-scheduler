package com.moonchase.outages_scheduler.runner;

import com.moonchase.outages_scheduler.service.ScheduleUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ManualTriggerRunner implements CommandLineRunner {

    @Autowired
    private ScheduleUpdateService scheduleUpdateService;

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && "update-calendars".equalsIgnoreCase(args[0])) {
            scheduleUpdateService.triggerManualUpdate();
        }
    }
}
