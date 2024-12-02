package com.moonchase.outages_scheduler.controller;

import com.moonchase.outages_scheduler.dto.EventDTO;
import com.moonchase.outages_scheduler.service.ScheduleUpdateService;
import com.moonchase.outages_scheduler.util.EventHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ScheduleUpdateController {

    @Autowired
    private ScheduleUpdateService scheduleUpdateService;

    @Autowired
    private EventHelper eventHelper;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Оновлення відбуваються кожні 30 хвилин. " +
                "Для запуску позачергового оновлення натисніть 'Оновити Дані'. Оновлення займає приблизно 10 секунд.");

        Map<String, List<EventDTO>> outages = new HashMap<>();

        try {
            outages.put("Група 3.1. Жидачів.", eventHelper.getThreeOneEvents());
            outages.put("Група 3.2. Станиля.", eventHelper.getThreeTwoEvents());
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
}
