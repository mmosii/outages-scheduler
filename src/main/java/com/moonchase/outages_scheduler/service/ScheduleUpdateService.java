package com.moonchase.outages_scheduler.service;

import com.moonchase.outages_scheduler.util.OCRProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScheduleUpdateService {

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private OCRProcessor ocrProcessor;

    private final Logger logger = LogManager.getLogger(ScheduleUpdateService.class);

    @Scheduled(cron = "0 0/30 * * * ?")
    public void updateCalendars() {
        this.logger.info("Оновлення графіків розпочато о " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        try {
            List<String> scheduleData = this.ocrProcessor.processImageFromURL("https://poweron.loe.lviv.ua/");
            final Map<LocalDate, Map<Integer, Boolean>> dataForGroupThreeOne = getDataForGroup(scheduleData, "3 1");
            this.logger.info("Група 3.1: " + dataForGroupThreeOne);
            final Map<LocalDate, Map<Integer, Boolean>> dataForGroupThreeTwo = getDataForGroup(scheduleData, "3 2");
            this.logger.info("Група 3.2: " + dataForGroupThreeTwo);
            this.googleCalendarService.updateCalendar("0f94eda0237411b6817e46db28b0fe1fa56458ef15cba55f21d1086c671eebc9@group.calendar.google.com", dataForGroupThreeTwo, "Станилі, група 3.2");
            this.googleCalendarService.updateCalendar("c041ec9de47a2af1c94483535724f907241f1f41d8ae6b293503462f3de0cf2f@group.calendar.google.com", dataForGroupThreeOne, "Жидачеві, група 3.1");
            this.logger.info("Оновлення графіків завершено о " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        } catch (Exception e) {
            this.logger.warn("Оновлення графіків не вдалось: " + e.getMessage());
        }
    }

    private Map<LocalDate, Map<Integer, Boolean>> getDataForGroup(List<String> scheduleData, String group) {
        Map<LocalDate, Map<Integer, Boolean>> results = new HashMap<>();
        for (String data : scheduleData) {
            String regex = "\\d{8}";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(data);
            if (matcher.find()) {
                String date = matcher.group();
                LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("ddMMyyyy"));
                LocalDate today = LocalDate.now();
                if (!parsedDate.isBefore(today)) {
                    final Map<Integer, Boolean> hoursMap = createHoursMap(data, group);
                    results.put(parsedDate, hoursMap);
                } else {
                    this.logger.warn("Дата " + parsedDate + " вже не актуальна");
                }
            } else {
                this.logger.warn("Актуальної дати не знайдено");
            }
        }
        return results;
    }

    public Map<Integer, Boolean> createHoursMap(String data, String group) {
        Map<Integer, Boolean> result = new HashMap<>(24);

        String regex = group + "\\s+\\d+\\s+([а-яА-ЯєЄіІїЇёЁ0-9\\s]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);

        if (matcher.find()) {
            String rowData = matcher.group(1).trim();

            Pattern wordPattern = Pattern.compile("(енергія|енергії)");
            Matcher wordMatcher = wordPattern.matcher(rowData);

            int hour = 0;
            while (wordMatcher.find() && hour < 24) {
                String word = wordMatcher.group();
                if ("енергія".equals(word)) {
                    result.put(hour, true);
                } else if ("енергії".equals(word)) {
                    result.put(hour, false);
                }
                hour++;
            }
        } else {
            this.logger.warn("Інформації про відключення групи " + group + " не знайдено");
        }
        return result;
    }

    public void triggerManualUpdate() {
        updateCalendars();
    }
}
