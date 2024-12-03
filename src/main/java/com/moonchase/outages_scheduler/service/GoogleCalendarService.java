package com.moonchase.outages_scheduler.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.moonchase.outages_scheduler.config.GoogleCalendarConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GoogleCalendarService {
    private final Logger logger = LogManager.getLogger(ScheduleUpdateService.class);

    private final Calendar googleCalendar = GoogleCalendarConfig.getCalendarService();
    private final ZoneId localZone = ZoneId.of("Europe/Kiev");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    public GoogleCalendarService() throws IOException, GeneralSecurityException {
    }

    public void updateCalendar(String calendarId, Map<LocalDate, Map<Integer, Boolean>> data, String group) throws IOException {
        this.deleteEvents(calendarId);

        final Set<LocalDateTime> validDateTimes = data.entrySet().stream()
                .flatMap(entry -> entry.getValue().entrySet().stream()
                        .filter(hourEntry -> Boolean.FALSE.equals(hourEntry.getValue()))
                        .map(hourEntry -> entry.getKey().atTime(hourEntry.getKey(), 0).atZone(localZone).toLocalDateTime()))
                .collect(Collectors.toSet());

        for (LocalDateTime startTime : validDateTimes) {
            LocalDateTime endTime = startTime.plusHours(1).atZone(localZone).toLocalDateTime();
            DateTime startDateTime = new DateTime(startTime.atZone(localZone).toInstant().toString());
            DateTime endDateTime = new DateTime(endTime.atZone(localZone).toInstant().toString());
            String creationDateTime = LocalDateTime.now().format(formatter);

            Event event = new Event()
                    .setSummary("Темрява поглинула " + group)
                    .setDescription("Заплановано виключення електроенергії " + startTime.format(DateTimeFormatter.ofPattern("dd:MM")) +
                            " о " + startTime.getHour() + " годині. Дані оновлено: " + creationDateTime)
                    .setStart(new EventDateTime().setDateTime(startDateTime).setTimeZone(localZone.getId()))
                    .setEnd(new EventDateTime().setDateTime(endDateTime).setTimeZone(localZone.getId()));

            googleCalendar.events().insert(calendarId, event).execute();
            this.logger.info("Створено нове відключення о " + startTime.format(formatter));
        }
    }

    private void deleteEvents(String calendarId) throws IOException {
        googleCalendar.events().list(calendarId).setSingleEvents(true).execute().getItems()
                .forEach(e -> {
                    try {
                        googleCalendar.events().delete(calendarId, e.getId()).execute();
                        this.logger.info("Видалено відключення о " +
                                ZonedDateTime.parse(e.getStart().getDateTime().toString())
                                        .format(formatter));
                    } catch (IOException ex) {
                        this.logger.error("Помилка при видалення відключення з Id " + e.getId() + ": " + ex.getMessage());
                    }
                });
    }

    public List<Event> getExistingOutages(String calendarId) throws IOException {
        Events events = googleCalendar.events().list(calendarId).setSingleEvents(true).execute();

        return events.getItems();
    }
}
