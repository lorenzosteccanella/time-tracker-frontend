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

/**
 * Controller for managing time tracking operations.
 */
@Controller
@RequestMapping("/timetracker")
public class TimeTrackerController {

    // Default number of records to fetch in one request
    @Value("${timetracker.length:20}")
    private int RECORDS_LENGTH;

    // Service for handling time tracking operations
    @Autowired
    private TimeTrackerService timeTrackerService;

    /**
     * Handles GET requests to the root URL.
     * @return the name of the view for the homepage
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Displays the form for viewing records.
     * @return the name of the view for the records viewing form
     */
    @GetMapping("/view-records")
    public String viewRecordsForm() {
        return "viewRecords";
    }

    /**
     * Creates a response map with formatted records and information about pagination.
     * @param timezone the user's timezone for formatting dates
     * @param records the list of time records
     * @return a map containing formatted records and pagination info
     */
    private Map<String, Object> createRecordsResponse(String timezone, List<TimeRecord> records) {
        // Determine if there are more records to fetch
        boolean hasMore = records.size() == RECORDS_LENGTH;

        // Format records for the response
        List<Map<String, String>> formattedRecords = records.stream().map(record -> {
            Map<String, String> formattedRecord = new HashMap<>();
            formattedRecord.put("email", record.getEmail());
            formattedRecord.put("start", DateTimeUtils.formatZonedDateTime(record.getStart(), timezone));
            formattedRecord.put("end", DateTimeUtils.formatZonedDateTime(record.getEnd(), timezone));
            return formattedRecord;
        }).collect(Collectors.toList());

        // Prepare response map
        Map<String, Object> response = new HashMap<>();
        response.put("records", formattedRecords);
        response.put("hasMore", hasMore);

        return response;
    }

    /**
     * Handles POST requests to fetch records based on email and timezone.
     * @param email the email to search records for
     * @param timezone the timezone for formatting the records
     * @param model the model to pass data to the view
     * @param session the HTTP session to store search parameters
     * @return the name of the view to display the records
     */
    @PostMapping("/view-records")
    public String getRecords(@RequestParam String email, @RequestParam String timezone, Model model, HttpSession session) {
        // Store email and timezone in the session
        session.setAttribute("email", email);
        session.setAttribute("timezone", timezone);
        session.setAttribute("offset", 0);

        // Fetch records from the service
        List<TimeRecord> records = timeTrackerService.getRecordsByEmail(email, 0, RECORDS_LENGTH);

        // Add records and pagination info to the model
        model.addAttribute("records", createRecordsResponse(timezone, records));

        // Display an error message if no records are found
        if (records.isEmpty()) {
            model.addAttribute("errorMessage", "No data available in the database for this query.");
        }
        return "viewRecords";
    }

    /**
     * Handles GET requests to fetch additional records for pagination.
     * @param session the HTTP session to retrieve search parameters and offset
     * @return a map containing additional records and pagination info
     */
    @GetMapping("/api/records")
    @ResponseBody
    public Map<String, Object> getMoreRecords(HttpSession session) {
        // Retrieve stored email, timezone, and offset from the session
        String email = (String) session.getAttribute("email");
        String timezone = (String) session.getAttribute("timezone");
        int offset = (int) session.getAttribute("offset");

        // Update offset for the next batch of records
        offset += RECORDS_LENGTH;
        session.setAttribute("offset", offset);

        // Validate email
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        // Fetch more records from the service
        List<TimeRecord> records = timeTrackerService.getRecordsByEmail(email, offset, RECORDS_LENGTH);

        // Return formatted records and pagination info
        return createRecordsResponse(timezone, records);
    }

    /**
     * Displays the form for creating a new time record.
     * @return the name of the view for the create record form
     */
    @GetMapping("/create-record")
    public String createRecordForm() {
        return "createRecord";
    }

    /**
     * Handles POST requests to create a new time record.
     * @param email the email associated with the record
     * @param start the start time of the record
     * @param end the end time of the record
     * @param timezone the timezone for the record
     * @param model the model to pass data to the view
     * @return the name of the view to display after record creation
     */
    @PostMapping("/create-record")
    public String createRecord(@RequestParam String email, @RequestParam String start, @RequestParam String end, @RequestParam String timezone, Model model) {
        try {
            // Parse and convert start and end times
            ZonedDateTime startZonedDateTime = DateTimeUtils.parseAndConvertToZonedDateTime(start, timezone);
            ZonedDateTime endZonedDateTime = DateTimeUtils.parseAndConvertToZonedDateTime(end, timezone);

            // Validate date range
            if (!DateTimeUtils.validateDateRange(startZonedDateTime, endZonedDateTime)) {
                model.addAttribute("errorMessage", "Invalid date range: start date must be before end date and dates cannot be in the future.");
                return "createRecord";
            }

            // Create and save the new time record
            TimeRecord record = new TimeRecord(email, startZonedDateTime, endZonedDateTime);
            timeTrackerService.createRecord(record);
            model.addAttribute("successMessage", "Record created successfully");
        } catch (Exception e) {
            // Handle errors and provide feedback
            model.addAttribute("errorMessage", "An error occurred: " + e.getMessage());
        }

        return "createRecord";
    }
}
