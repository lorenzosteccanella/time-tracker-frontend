package com.timetracker.frontend.service;
import com.timetracker.frontend.model.TimeRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TimeTrackerService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8080";

    public List<TimeRecord> getRecordsByEmail(String email, int offset, int length) {
        String url = String.format("%s/records?email=%s&offset=%d&length=%d", apiUrl, email, offset, length);

        System.out.println("Sending GET request to " + url);
        System.out.println("email: " + email);
        System.out.println("offset: " + offset);
        System.out.println("length: " + length);

        TimeRecord[] array = restTemplate.getForObject(url, TimeRecord[].class);

        assert array != null;

        // remove all null elements from the array
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
