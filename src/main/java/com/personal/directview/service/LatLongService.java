package com.personal.directview.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LatLongService {

    @Value("${google.api.key}")
    private String apiKey;

    @Value("${google.api.opformat}")
    private String addOutputFormat;

    private final RestTemplate restTemplate;

    public LatLongService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Build the URI for the Geocoding API request
    private URI buildUri(String address) {
        return UriComponentsBuilder
                .fromHttpUrl("https://maps.googleapis.com/maps/api/geocode/" + addOutputFormat)
                .queryParam("address", address)
                .queryParam("key", apiKey)
                .build()
                .toUri();
    }

    // Fetch Geocode data from the API
    private Map<String, Object> fetchGeocodeData(String address) {
        URI uri = buildUri(address);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
        if (response == null || !"OK".equals(response.get("status"))) {
            throw new RuntimeException("Error fetching geocoded address: " + (response != null ? response.get("status") : "No response"));
        }
        return response;
    }

    // Get latitude and longitude based on the address
    public Map<String, Object> getLatLong(String address) {
        Map<String, Object> response = fetchGeocodeData(address);
        Map<String, Object> result = new HashMap<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        if (results.isEmpty()) {
            throw new RuntimeException("No results found for the given address");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> geometry = (Map<String, Object>) results.get(0).get("geometry");
        @SuppressWarnings("unchecked")
        Map<String, Object> location = (Map<String, Object>) geometry.get("location");

        result.put("lat", location.get("lat"));
        result.put("long", location.get("lng"));

        return result;
    }
}