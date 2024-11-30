package com.moonchase.outages_scheduler.dto;

public class EventDTO {
    private String group;
    private String start;
    private String end;
    private String description;

    public EventDTO(String group, String start, String end, String description) {
        this.group = group;
        this.start = start;
        this.end = end;
        this.description = description;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
