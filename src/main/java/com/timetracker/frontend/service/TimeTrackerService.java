package com.timetracker.frontend.service;

import com.timetracker.frontend.model.TimeRecord;
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

@Service
public class TimeTrackerService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8080";
    private final Cache<String, List<TimeRecord>> cache;

    public TimeTrackerService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)  // Evict entries 10 minutes after last access
                .maximumSize(1000)                        // Limit cache size to 1000 entries
                .build();
    }

    public List<TimeRecord> getRecordsByEmail(String email, int offset, int length) {
        String cacheKey = String.format("%s:%d:%d", email, offset, length);

        // Check if the data is present in the cache
        List<TimeRecord> cachedRecords = cache.getIfPresent(cacheKey);
        if (cachedRecords != null) {
            System.out.println("Retrieved records from cache for key: " + cacheKey);
            return cachedRecords;
        } else {
            // If not present in the cache, fetch from API and cache the result
            List<TimeRecord> records = fetchRecordsFromApi(email, offset, length);
            cache.put(cacheKey, records);
            System.out.println("Added records to cache for key: " + cacheKey);
            return records;
        }
    }

    private List<TimeRecord> fetchRecordsFromApi(String email, int offset, int length) {
        String url = String.format("%s/records?email=%s&offset=%d&length=%d", apiUrl, email, offset, length);

        System.out.println("Sending GET request to " + url);
        System.out.println("email: " + email);
        System.out.println("offset: " + offset);
        System.out.println("length: " + length);

        TimeRecord[] array = restTemplate.getForObject(url, TimeRecord[].class);

        if (array == null) {
            return List.of(); // Return empty list if API returns null
        }

        // Remove all null elements from the array
        array = Arrays.stream(array).filter(Objects::nonNull).toArray(TimeRecord[]::new);

        return Arrays.stream(array).collect(Collectors.toList());
    }

    public void createRecord(TimeRecord record) {
        String url = apiUrl + "/records";

        System.out.println("Sending POST request to " + url);
        System.out.println("Email: " + record.getEmail());
        System.out.println("Start: " + record.getStartPostReq());
        System.out.println("End: " + record.getEndPostReq());

        // Prepare the request body
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", record.getEmail());
        formData.add("start", record.getStartPostReq());
        formData.add("end", record.getEndPostReq());

        try {
            // Perform POST request with form data
            restTemplate.postForObject(url, formData, String.class);

            // Invalidate cache entries related to this email
            invalidateCacheForEmail(record.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void invalidateCacheForEmail(String email) {
        // Invalidate all cache entries for the given email
        cache.asMap().keySet().removeIf(key -> key.startsWith(email));
        System.out.println("Invalidated cache for email: " + email);
    }
}
