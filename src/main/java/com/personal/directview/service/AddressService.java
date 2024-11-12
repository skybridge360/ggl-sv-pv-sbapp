package com.personal.directview.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AddressService {

    @Value("${google.api.key}")
    private String apiKey;

    @Value("${google.api.opformat}")
    private String addOutputFormat;

    private final RestTemplate restTemplate;

    public AddressService(RestTemplate restTemplate) {
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

    // New method to return the complete raw response from the Geocoding API
    public Map<String, Object> getRawAddressData(String address) {
        return fetchGeocodeData(address);  // Simply return the raw response
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

    // Get full address components and map them to readable labels
    public Map<String, String> getAddressComponents(String address) {
        Map<String, Object> response = fetchGeocodeData(address);
        Map<String, String> addressComponentsMap = new HashMap<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        if (results.isEmpty()) {
            throw new RuntimeException("No results found for the given address");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> addressComponents = (List<Map<String, Object>>) results.get(0).get("address_components");

        // Loop through the components and map to custom labels
        for (Map<String, Object> component : addressComponents) {
            @SuppressWarnings("unchecked")
            List<String> types = (List<String>) component.get("types");

            // Map components to specific labels based on types
            for (String type : types) {
                switch (type) {
                    case "establishment","point_of_interest":
                        addressComponentsMap.put("building", (String) component.get("long_name"));
                        break;
                    case "street_number":
                        addressComponentsMap.put("house_number", (String) component.get("long_name"));
                        break;
                    case "route":
                        addressComponentsMap.put("street", (String) component.get("long_name"));
                        break;
                    case "locality":
                        addressComponentsMap.put("city", (String) component.get("long_name"));
                        break;
                    case "administrative_area_level_1":
                        addressComponentsMap.put("state", (String) component.get("long_name"));
                        break;
                    case "administrative_area_level_2":
                        addressComponentsMap.put("county", (String) component.get("long_name"));
                        break;
                    case "postal_code":
                        addressComponentsMap.put("postal_code", (String) component.get("long_name"));
                        break;
                    case "country":
                        addressComponentsMap.put("country", (String) component.get("long_name"));
                        break;
                    case "premise":
                        addressComponentsMap.put("premise", (String) component.get("long_name"));
                        break;
                    case "subpremise":
                        addressComponentsMap.put("apartment_number", (String) component.get("long_name"));
                        break;
                    case "neighborhood":
                        addressComponentsMap.put("neighborhood", (String) component.get("long_name"));
                        break;
                    case "political","sublocality","sublocality_level_2":
                        addressComponentsMap.put("locality", (String) component.get("long_name"));
                        break;
                    default:
                        // Ignore other types or add extra if needed
                        break;
                }
            }
        }
        return addressComponentsMap;
    }
}
