package com.timetracker.frontend.controller;

import com.timetracker.frontend.model.TimeRecord;
import com.timetracker.frontend.service.TimeTrackerService;
import com.timetracker.frontend.util.DateTimeUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/timetracker")
public class TimeTrackerController {

    @Value("${timetracker.length:20}")
    private int RECORDS_LENGTH;

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

    private Map<String, Object> createRecordsResponse(String timezone, List<TimeRecord> records) {
        boolean hasMore = records.size() == RECORDS_LENGTH;

        List<Map<String, String>> formattedRecords = records.stream().map(record -> {
            Map<String, String> formattedRecord = new HashMap<>();
            formattedRecord.put("email", record.getEmail());
            formattedRecord.put("start", DateTimeUtils.formatZonedDateTime(record.getStart(), timezone));
            formattedRecord.put("end", DateTimeUtils.formatZonedDateTime(record.getEnd(), timezone));
            return formattedRecord;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("records", formattedRecords);
        response.put("hasMore", hasMore);

        return response;
    }

    @PostMapping("/view-records")
    public String getRecords(@RequestParam String email, @RequestParam String timezone, Model model, HttpSession session) {
        session.setAttribute("email", email);
        session.setAttribute("timezone", timezone);
        session.setAttribute("offset", 0);

        List<TimeRecord> records = timeTrackerService.getRecordsByEmail(email, 0, RECORDS_LENGTH);

        model.addAttribute("records", createRecordsResponse(timezone, records));

        // if the records are empty, display an error message
        if (records.isEmpty()) {
            model.addAttribute("errorMessage", "No data available in the database for this query.");
        }
        return "viewRecords";
    }

    @GetMapping("/api/records")
    @ResponseBody
    public Map<String, Object> getMoreRecords(HttpSession session) {
        String email = (String) session.getAttribute("email");
        String timezone = (String) session.getAttribute("timezone");

        int offset = (int) session.getAttribute("offset");
        offset += RECORDS_LENGTH;
        session.setAttribute("offset", offset);

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        List<TimeRecord> records = timeTrackerService.getRecordsByEmail(email, offset, RECORDS_LENGTH);

        return createRecordsResponse(timezone, records);
    }

    @GetMapping("/create-record")
    public String createRecordForm() {
        return "createRecord";
    }

    @PostMapping("/create-record")
    public String createRecord(@RequestParam String email, @RequestParam String start, @RequestParam String end, @RequestParam String timezone, Model model) {
        try {
            ZonedDateTime startZonedDateTime = DateTimeUtils.parseAndConvertToZonedDateTime(start, timezone);
            ZonedDateTime endZonedDateTime = DateTimeUtils.parseAndConvertToZonedDateTime(end, timezone);

            if (!DateTimeUtils.validateDateRange(startZonedDateTime, endZonedDateTime)) {
                model.addAttribute("errorMessage", "Invalid date range: start date must be before end date and dates cannot be in the future.");
                return "createRecord";
            }

            TimeRecord record = new TimeRecord(email, startZonedDateTime, endZonedDateTime);
            timeTrackerService.createRecord(record);
            model.addAttribute("successMessage", "Record created successfully");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred: " + e.getMessage());
        }

        return "createRecord";
    }
}