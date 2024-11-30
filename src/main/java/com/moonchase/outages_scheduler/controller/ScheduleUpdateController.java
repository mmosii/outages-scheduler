package com.moonchase.outages_scheduler.controller;

import com.google.api.services.calendar.model.Event;
import com.moonchase.outages_scheduler.dto.EventDTO;
import com.moonchase.outages_scheduler.mapper.EventMapper;
import com.moonchase.outages_scheduler.service.GoogleCalendarService;
import com.moonchase.outages_scheduler.service.ScheduleUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ScheduleUpdateController {

    @Autowired
    private ScheduleUpdateService scheduleUpdateService;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private EventMapper eventMapper;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Оновлення відбуваються кожні 30 хвилин. " +
                "Для запуску позачергового оновлення натисніть 'Оновити Дані'. Оновлення займає приблизно 10 секунд.");

        Map<String, List<EventDTO>> outages = new HashMap<>();

        try {
            List<Event> group3_1Events = googleCalendarService.getExistingOutages("c041ec9de47a2af1c94483535724f907241f1f41d8ae6b293503462f3de0cf2f@group.calendar.google.com");
            List<Event> group3_2Events = googleCalendarService.getExistingOutages("0f94eda0237411b6817e46db28b0fe1fa56458ef15cba55f21d1086c671eebc9@group.calendar.google.com");

            outages.put("Група 3.1. Жидачів.", sortEventsByStartTime(eventMapper.mapEventsToDTO(group3_1Events, "Group 3.1")));
            outages.put("Група 3.2. Станиля.", sortEventsByStartTime(eventMapper.mapEventsToDTO(group3_2Events, "Group 3.2")));
        } catch (Exception e) {
            model.addAttribute("error", "Помилка при завантаженні таблиці: " + e.getMessage());
        }

        model.addAttribute("outages", outages);

        return "index";
    }

    @PostMapping("/trigger-update")
    public String triggerUpdate(Model model) {
        try {
            scheduleUpdateService.triggerManualUpdate();
            model.addAttribute("message", "Оновлення відбулось успішно");
        } catch (Exception e) {
            model.addAttribute("message", "Помилка при оновленні " + e.getMessage());
        }
        return "trigger-update";
    }

    private List<EventDTO> sortEventsByStartTime(List<EventDTO> events) {
        return events.stream()
                .sorted(Comparator.comparing(EventDTO::getStart))
                .collect(Collectors.toList());
    }
}
