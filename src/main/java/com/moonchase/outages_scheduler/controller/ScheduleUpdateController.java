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
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

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
            outages.put("Група 1.1.", eventHelper.getOneOneEvents());
            outages.put("Група 1.2.", eventHelper.getOneTwoEvents());
            outages.put("Група 2.1.", eventHelper.getTwoOneEvents());
            outages.put("Група 2.2.", eventHelper.getTwoTwoEvents());
        } catch (Exception e) {
            model.addAttribute("error", "Помилка при завантаженні таблиці: " + e.getMessage());
        }
        final Map<String, List<EventDTO>> sortedEntries = getSortedEntries(outages);
        model.addAttribute("outages", sortedEntries);

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

    public Map<String, List<EventDTO>> getSortedEntries(Map<String, List<EventDTO>> outages) {
        return outages.entrySet()
                .stream()
                .sorted((e1, e2) -> {
                    String key1 = e1.getKey();
                    String key2 = e2.getKey();

                    String[] split1 = key1.split(" ");
                    String[] split2 = key2.split(" ");
                    String num1 = split1[0];
                    String num2 = split2[0];

                    int result = num1.compareTo(num2);
                    if (result == 0 && split1.length > 1 && split2.length > 1) {
                        result = split1[1].compareTo(split2[1]);
                    }
                    return result;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}
