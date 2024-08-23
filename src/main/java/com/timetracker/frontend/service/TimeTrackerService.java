package com.timetracker.frontend.service;

import com.timetracker.frontend.model.TimeRecord;
import com.timetracker.frontend.util.DateTimeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for managing time records, including fetching records from the API
 * and caching results to improve performance.
 */
@Service
public class TimeTrackerService {

    // REST template for making HTTP requests
    private final RestTemplate restTemplate = new RestTemplate();

    // URL of the backend service, configurable via application properties
    @Value("${timetracker.backend.url:http://timetracker-backend:8080}")
    private String APIURL;

    // Cache to store records for a given email, offset, and length
    private final Cache<String, List<TimeRecord>> cache;

    /**
     * Constructor initializes the cache with expiration and size settings.
     */
    public TimeTrackerService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)  // Evict entries 10 minutes after last access
                .maximumSize(1000)                        // Limit cache size to 1000 entries
                .build();
    }

    /**
     * Retrieves records by email, with pagination support. Checks the cache first
     * before making an API call.
     * @param email the email to search for
     * @param offset the starting point of the records to fetch
     * @param length the number of records to fetch
     * @return a list of time records
     */
    public List<TimeRecord> getRecordsByEmail(String email, int offset, int length) {
        // Construct a unique cache key based on email, offset, and length
        String cacheKey = String.format("%s:%d:%d", email, offset, length);

        // Check if records are present in the cache
        List<TimeRecord> cachedRecords = cache.getIfPresent(cacheKey);
        if (cachedRecords != null) {
            return cachedRecords;
        } else {
            // Fetch records from API if not in cache
            List<TimeRecord> records = fetchRecordsFromApi(email, offset, length);
            // Cache the records if they are not empty
            if (!records.isEmpty()){
                cache.put(cacheKey, records);
            }
            return records;
        }
    }

    /**
     * Fetches records from the backend API.
     * @param email the email to search for
     * @param offset the starting point of the records to fetch
     * @param length the number of records to fetch
     * @return a list of time records
     */
    private List<TimeRecord> fetchRecordsFromApi(String email, int offset, int length) {
        // Construct the URL for the API request
        String url = String.format("%s/records?email=%s&offset=%d&length=%d", APIURL, email, offset, length);

        // Send GET request and receive response as an array of TimeRecord
        TimeRecord[] array = restTemplate.getForObject(url, TimeRecord[].class);

        // Return an empty list if the response is null
        if (array == null) {
            return List.of();
        }

        // Remove null elements from the array and convert to list
        array = Arrays.stream(array).filter(Objects::nonNull).toArray(TimeRecord[]::new);

        return Arrays.stream(array).collect(Collectors.toList());
    }

    /**
     * Creates a new time record by sending a POST request to the backend API.
     * @param record the time record to create
     */
    public void createRecord(TimeRecord record) {
        // Construct the URL for the API request
        String url = APIURL + "/records";

        // Prepare the request body as form data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", record.getEmail());
        formData.add("start", DateTimeUtils.formatForPostRequest(record.getStart()));
        formData.add("end", DateTimeUtils.formatForPostRequest(record.getEnd()));

        try {
            // Send POST request with form data
            restTemplate.postForObject(url, formData, String.class);

            // Invalidate cache entries related to this email
            invalidateCacheForEmail(record.getEmail());
        } catch (Exception e) {
            // Print stack trace for any errors during the request
            e.printStackTrace();
        }
    }

    /**
     * Invalidates all cache entries for the given email.
     * @param email the email for which to invalidate cache entries
     */
    private void invalidateCacheForEmail(String email) {
        // Remove all cache entries where the key starts with the given email
        cache.asMap().keySet().removeIf(key -> key.startsWith(email));
    }
}
