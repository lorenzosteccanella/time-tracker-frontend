package com.timetracker.frontend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.timetracker.frontend.util.DateTimeUtils;

import java.time.ZonedDateTime;

/**
 * The TimeRecord class represents a time entry for an employee, including
 * their email, the start time, and the end time of the recorded session.
 */
public class TimeRecord {
    private String email;
    private ZonedDateTime start;
    private ZonedDateTime end;

    /**
     * Constructs a TimeRecord object using string representations of the start and end times.
     * The input strings are parsed into ZonedDateTime objects.
     *
     * @param email the email of the employee
     * @param start the start time as a string
     * @param end   the end time as a string
     */
    public TimeRecord(
            @JsonProperty("email") String email,
            @JsonProperty("start") String start,
            @JsonProperty("end") String end) {
        this.email = email;
        this.start = DateTimeUtils.parseStringToZonedDateTime(start);
        this.end = DateTimeUtils.parseStringToZonedDateTime(end);
    }

    /**
     * Constructs a TimeRecord object using ZonedDateTime objects for start and end times.
     *
     * @param email the email of the employee
     * @param start the start time as a ZonedDateTime object
     * @param end   the end time as a ZonedDateTime object
     */
    public TimeRecord(String email, ZonedDateTime start, ZonedDateTime end) {
        this.email = email;
        this.start = start;
        this.end = end;
    }

    /**
     * Returns a string representation of the TimeRecord object.
     *
     * @return a string containing the email, start time, and end time
     */
    @Override
    public String toString() {
        return "TimeRecord{" +
                "email='" + email + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                '}';
    }

    /**
     * Gets the email associated with this time record.
     *
     * @return the email of the employee
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the start time of this time record.
     *
     * @return the start time as a ZonedDateTime object
     */
    public ZonedDateTime getStart() {
        return start;
    }

    /**
     * Gets the end time of this time record.
     *
     * @return the end time as a ZonedDateTime object
     */
    public ZonedDateTime getEnd() {
        return end;
    }
}
