package com.moonchase.outages_scheduler.util;

import com.google.api.services.calendar.model.Event;
import com.moonchase.outages_scheduler.dto.EventDTO;
import com.moonchase.outages_scheduler.mapper.EventMapper;
import com.moonchase.outages_scheduler.service.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EventHelper {
    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    public List<EventDTO> getOneOneEvents() throws IOException {
        return getEvents("ce4733f00e0b293cf8c59b87c6f049c30c2e39c7f3b682862165d9a31aaa8622@group.calendar.google.com", "1.1");
    }

    public List<EventDTO> getOneTwoEvents() throws IOException {
        return getEvents("ece05f5cf981f00fb6c62c135ce12c0c44a856883ea927392ce2c4d1ac142411@group.calendar.google.com", "1.2");
    }

    public List<EventDTO> getTwoOneEvents() throws IOException {
        return getEvents("81023f51f71667bf7f41fa9d721575b628c3607cb6e4b9863ee5d2474c04fc4d@group.calendar.google.com", "2.1");
    }

    public List<EventDTO> getTwoTwoEvents() throws IOException {
        return getEvents("d3021868c14416b5cd4e34bfcee29b490a5f79dc9828ee710a385d486ae22b5b@group.calendar.google.com", "2.2");
    }

    public List<EventDTO> getThreeOneEvents() throws IOException {
        return getEvents("c041ec9de47a2af1c94483535724f907241f1f41d8ae6b293503462f3de0cf2f@group.calendar.google.com", "3.1");
    }

    public List<EventDTO> getThreeTwoEvents() throws IOException {
        return getEvents("0f94eda0237411b6817e46db28b0fe1fa56458ef15cba55f21d1086c671eebc9@group.calendar.google.com", "3.2");
    }

    public String formatEventsToMessage(List<EventDTO> events) {
        if (events == null || events.isEmpty()) {
            return "На даний момент немає запланованих відключень.";
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDateTime now = LocalDateTime.now();

        Map<String, Map<LocalDate, List<EventDTO>>> groupedEvents = events.stream()
                .filter(event -> LocalDateTime.parse(event.getStart(), dateTimeFormatter).isAfter(now))
                .collect(Collectors.groupingBy(
                        EventDTO::getGroup,
                        Collectors.groupingBy(event -> LocalDate.parse(event.getStart(), dateTimeFormatter))));

        return groupedEvents.entrySet().stream()
                .map(groupEntry -> {
                    String group = groupEntry.getKey();
                    return groupEntry.getValue().entrySet().stream()
                            .map(dateEntry -> {
                                LocalDate date = dateEntry.getKey();
                                List<EventDTO> sortedEvents = dateEntry.getValue().stream()
                                        .sorted(Comparator.comparing(event -> LocalDateTime.parse(event.getStart(), dateTimeFormatter)))
                                        .collect(Collectors.toList());

                                String timeSlots = mergeConsecutiveEvents(sortedEvents, dateTimeFormatter, timeFormatter);

                                return String.format("Група %s.     %s\n%s", group, date.format(dateFormatter), timeSlots);
                            })
                            .collect(Collectors.joining("\n\n"));
                })
                .collect(Collectors.joining("\n\n"));
    }

    private String mergeConsecutiveEvents(List<EventDTO> events, DateTimeFormatter dateTimeFormatter, DateTimeFormatter timeFormatter) {
        StringBuilder mergedTimeSlots = new StringBuilder();
        LocalDateTime mergedStart = null;
        LocalDateTime mergedEnd = null;

        for (EventDTO event : events) {
            LocalDateTime eventStart = LocalDateTime.parse(event.getStart(), dateTimeFormatter);
            LocalDateTime eventEnd = LocalDateTime.parse(event.getEnd(), dateTimeFormatter);

            if (mergedStart == null) {
                mergedStart = eventStart;
                mergedEnd = eventEnd;
            } else {
                if (eventStart.equals(mergedEnd)) {
                    mergedEnd = eventEnd;
                } else {
                    mergedTimeSlots.append(String.format("%s-%s\n", mergedStart.toLocalTime().format(timeFormatter), mergedEnd.toLocalTime().format(timeFormatter)));
                    mergedStart = eventStart;
                    mergedEnd = eventEnd;
                }
            }
        }

        if (mergedStart != null) {
            mergedTimeSlots.append(String.format("%s-%s", mergedStart.toLocalTime().format(timeFormatter), mergedEnd.toLocalTime().format(timeFormatter)));
        }

        return mergedTimeSlots.toString();
    }

    private List<EventDTO> getEvents(String calendarId, String group) throws IOException {
        List<Event> group3_1Events = googleCalendarService.getExistingOutages(calendarId);
        return sortEventsByStartTime(eventMapper.mapEventsToDTO(group3_1Events, group));
    }

    private List<EventDTO> sortEventsByStartTime(List<EventDTO> events) {
        return events.stream()
                .filter(event -> LocalDateTime.parse(event.getStart(), DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")).isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(event -> LocalDateTime.parse(event.getStart(), DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"))))
                .collect(Collectors.toList());
    }
}
