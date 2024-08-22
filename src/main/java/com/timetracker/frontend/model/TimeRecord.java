package com.timetracker.frontend.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeRecord {
    private String email;
    private ZonedDateTime startZoned;
    private ZonedDateTime endZoned;
    private static final DateTimeFormatter string2zonedDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final DateTimeFormatter zonedDateTime2string = DateTimeFormatter.ofPattern("yyyy-MM-dd - HH:mm");
    private static final DateTimeFormatter localDateTime2string = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public TimeRecord(
            @JsonProperty("email") String email,
            @JsonProperty("start") String start,
            @JsonProperty("end") String end) {
        this.email = email;
        this.startZoned = ZonedDateTime.parse(start, string2zonedDateTime);
        this.endZoned = ZonedDateTime.parse(end, string2zonedDateTime);
    }

    public TimeRecord(String email, ZonedDateTime start, ZonedDateTime end) {
        this.email = email;
        this.startZoned = start;
        this.endZoned = end;
    }

    @Override
    public String toString() {
        return "TimeRecord{" +
                "email='" + email + '\'' +
                ", start='" + startZoned + '\'' +
                ", end='" + endZoned + '\'' +
                '}';
    }

    public String getStartString(String timezone) {
        // first let's convert the global time to the correct timezone
        // then return a string including a Date - Time
        return startZoned.withZoneSameInstant(ZoneId.of(timezone)).format(zonedDateTime2string);
    }

    public String getEndString(String timezone) {
        return endZoned.withZoneSameInstant(ZoneId.of(timezone)).format(zonedDateTime2string);
    }

    public String getStartPostReq(){
        // convert the global time to local time at UTC 0 timezone
        return startZoned.withZoneSameInstant(ZoneId.of("UTC")).format(localDateTime2string);
    }

    public String getEndPostReq(){
        return endZoned.withZoneSameInstant(ZoneId.of("UTC")).format(localDateTime2string);
    }

    public String getEmail() {
        return email;
    }
}

