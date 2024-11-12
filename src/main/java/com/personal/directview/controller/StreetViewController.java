package com.personal.directview.controller;

import com.personal.directview.service.LatLongService;
import com.personal.directview.service.StreetViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/street-view")
public class StreetViewController {

    private final StreetViewService streetViewService;
    private final LatLongService latLongService;

    @Autowired
    public StreetViewController(StreetViewService streetViewService, LatLongService latLongService) {
        this.streetViewService = streetViewService;
        this.latLongService = latLongService;
    }

    @GetMapping("/images")
    public ResponseEntity<?> downloadStreetViewImages(@RequestBody String location) {
        try {
            // Step 1: Fetch latitude and longitude for the provided location
            Map<String, Object> latLong = latLongService.getLatLong(location);
            double latitude = (double) latLong.get("lat");
            double longitude = (double) latLong.get("long");

            // Step 2: Download Street View images using the location, latitude, and longitude
            List<String> filePaths = streetViewService.downloadStreetViewImages(location, latitude, longitude);

            // Return the list of saved file paths
            return ResponseEntity.ok(filePaths);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error downloading images: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error fetching latitude and longitude: " + e.getMessage());
        }
    }
}
