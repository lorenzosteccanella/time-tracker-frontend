package com.timetracker.frontend.controller;

import com.timetracker.frontend.model.TimeRecord;
import com.timetracker.frontend.service.TimeTrackerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


@Controller
@RequestMapping("/timetracker")
public class TimeTrackerController {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Autowired
    private TimeTrackerService timeTrackerService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/view-records")
    public String viewRecordsForm() {
        return "viewRecords";
    }

    @PostMapping("/view-records")
    public String viewRecords(@RequestParam String email,
                              @RequestParam int length,
                              @RequestParam String timezone,
                              Model model) {
        model.addAttribute("records", timeTrackerService.getRecordsByEmail(email, length));
        model.addAttribute("timezone", timezone);
        return "viewRecords";
    }

    @GetMapping("/create-record")
    public String createRecordForm() {
        return "createRecord";
    }

    @PostMapping("/create-record")
    public String createRecord(@RequestParam String email,
                               @RequestParam String start,
                               @RequestParam String end,
                               @RequestParam String timezone,
                               Model model) {

        // check that the string is in the correct format
        try{
            LocalDateTime startDateTime = LocalDateTime.parse(start, formatter);
            LocalDateTime endDateTime = LocalDateTime.parse(end, formatter);

            // given the UTC timezone in format +HH:MM or -HH:MM convert it to ZoneId
            ZoneId zoneId = ZoneId.of(timezone);

            ZonedDateTime startZonedDateTime = startDateTime.atZone(zoneId);
            ZonedDateTime endZonedDateTime = endDateTime.atZone(zoneId);

            // let's add a check to ensure that the start time is before the end time and that the dates are not in the future
            if (startZonedDateTime.isAfter(endZonedDateTime) || startZonedDateTime.isAfter(ZonedDateTime.now()) || endZonedDateTime.isAfter(ZonedDateTime.now())) {
                model.addAttribute("errorMessage", "Invalid date range, please ensure that the start date is before the end date and that the dates are not in the future");
                return "createRecord";
            }

            TimeRecord record = new TimeRecord(email, startZonedDateTime, endZonedDateTime);
            // add a try catch block to handle the exception
            try {
                timeTrackerService.createRecord(record);
                model.addAttribute("successMessage", "Record created successfully");
                return "createRecord";
            } catch (Exception e) {
                model.addAttribute("errorMessage", "An error occurred while posting the record");
                return "createRecord";
            }

        } catch (DateTimeParseException e) {
            model.addAttribute("errorMessage", "Invalid date format, please use the format yyyy-MM-dd'T'HH:mm");
            return "createRecord";
        }
    }
}
