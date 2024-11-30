package com.moonchase.outages_scheduler.mapper;

import com.google.api.services.calendar.model.Event;
import com.moonchase.outages_scheduler.dto.EventDTO;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.client.util.DateTime;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm");

    public List<EventDTO> mapEventsToDTO(List<Event> events, String group) {
        return events.stream()
                .map(event -> new EventDTO(
                        group,
                        formatDate(event.getStart().getDateTime()),
                        formatDate(event.getEnd().getDateTime()),
                        event.getDescription()))
                .collect(Collectors.toList());
    }

    private String formatDate(DateTime dateTime) {
        if (dateTime != null) {
            return DATE_FORMAT.format(dateTime.getValue());
        }
        return null;
    }
}
