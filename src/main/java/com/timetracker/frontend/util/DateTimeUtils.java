package com.timetracker.frontend.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for handling date and time conversions, parsing, and formatting.
 */
public class DateTimeUtils {

    // Formatter for parsing strings into ZonedDateTime with a specific pattern.
    private static final DateTimeFormatter STRING_TO_ZONED_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    // Formatter for converting ZonedDateTime to a formatted string for display.
    private static final DateTimeFormatter ZONED_DATETIME_TO_STRING = DateTimeFormatter.ofPattern("yyyy-MM-dd - HH:mm");

    // Formatter for converting LocalDateTime to a formatted string, typically for requests.
    private static final DateTimeFormatter LOCAL_DATETIME_TO_STRING = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Private constructor to prevent instantiation of this utility class.
    private DateTimeUtils() {
    }

    /**
     * Parses a string into a LocalDateTime using the ISO_LOCAL_DATE_TIME format.
     *
     * @param dateTimeStr the date-time string to parse
     * @return a LocalDateTime object
     * @throws DateTimeParseException if the string cannot be parsed
     */
    public static LocalDateTime parseStringToLocalDateTime(String dateTimeStr) throws DateTimeParseException {
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Parses a string into a ZonedDateTime using a specific pattern.
     *
     * @param dateTimeStr the date-time string to parse
     * @return a ZonedDateTime object
     * @throws DateTimeParseException if the string cannot be parsed
     */
    public static ZonedDateTime parseStringToZonedDateTime(String dateTimeStr) throws DateTimeParseException {
        return ZonedDateTime.parse(dateTimeStr, STRING_TO_ZONED_DATETIME);
    }

    /**
     * Converts a LocalDateTime into a ZonedDateTime based on the provided timezone.
     *
     * @param localDateTime the LocalDateTime to convert
     * @param timezone      the target timezone as a string
     * @return a ZonedDateTime object
     * @throws DateTimeException if the timezone is invalid
     */
    public static ZonedDateTime convertToZonedDateTime(LocalDateTime localDateTime, String timezone) throws DateTimeException {
        ZoneId zoneId = ZoneId.of(timezone);
        return localDateTime.atZone(zoneId);
    }

    /**
     * Validates if a date range is valid. Ensures the start time is before the end time,
     * and both are not in the future.
     *
     * @param start the start time as a ZonedDateTime
     * @param end   the end time as a ZonedDateTime
     * @return true if the date range is valid, false otherwise
     */
    public static boolean validateDateRange(ZonedDateTime start, ZonedDateTime end) {
        ZonedDateTime now = ZonedDateTime.now();
        return !start.isAfter(end) && !start.isAfter(now) && !end.isAfter(now);
    }

    /**
     * Formats a ZonedDateTime into a string based on the specified timezone.
     *
     * @param zonedDateTime the ZonedDateTime to format
     * @param timezone      the target timezone for formatting
     * @return a formatted string representation of the ZonedDateTime
     */
    public static String formatZonedDateTime(ZonedDateTime zonedDateTime, String timezone) {
        return zonedDateTime.withZoneSameInstant(ZoneId.of(timezone)).format(ZONED_DATETIME_TO_STRING);
    }

    /**
     * Formats a ZonedDateTime for use in a POST request, converting it to UTC and formatting it.
     *
     * @param zonedDateTime the ZonedDateTime to format
     * @return a formatted string representation in UTC
     */
    public static String formatForPostRequest(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")).format(LOCAL_DATETIME_TO_STRING);
    }

    /**
     * Parses a date-time string into a LocalDateTime and converts it to a ZonedDateTime
     * based on the provided timezone.
     *
     * @param dateTimeStr the date-time string to parse
     * @param timezone    the target timezone for conversion
     * @return a ZonedDateTime object
     * @throws DateTimeParseException if the string cannot be parsed
     */
    public static ZonedDateTime parseAndConvertToZonedDateTime(String dateTimeStr, String timezone) throws DateTimeParseException {
        // Parse the string into a LocalDateTime
        LocalDateTime localDateTime = parseStringToLocalDateTime(dateTimeStr);

        // Convert the LocalDateTime to ZonedDateTime using the specified timezone
        return convertToZonedDateTime(localDateTime, timezone);
    }
}