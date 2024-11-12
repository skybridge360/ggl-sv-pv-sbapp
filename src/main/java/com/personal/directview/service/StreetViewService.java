package com.personal.directview.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class StreetViewService {

    @Value("${google.api.key}")
    private String apiKey;

    @Value("${google.sv.image.width}")
    private String svImgWidth;

    @Value("${google.sv.image.height}")
    private String svImgHeight;

    @Value("${google.sv.image.pitch}")
    private String svImgPitch;

    @Value("${download.folder.path}")
    private String downloadFolderPath;  // Base folder for downloads

    private final RestTemplate restTemplate;
    private final PanoramicViewService panoramicViewService;

    public StreetViewService(RestTemplate restTemplate, PanoramicViewService panoramicViewService) {
        this.restTemplate = restTemplate;
        this.panoramicViewService = panoramicViewService;
    }

    // Build the URI for the Street View API request with a specific heading
    private URI buildSVUri(String location, int heading) {
        return UriComponentsBuilder
                .fromHttpUrl("https://maps.googleapis.com/maps/api/streetview")
                .queryParam("size", svImgWidth + "x" + svImgHeight)
                .queryParam("location", location)
                .queryParam("heading", heading)
                .queryParam("pitch", svImgPitch)
                .queryParam("key", apiKey)
                .build()
                .toUri();
    }

    // Generate a unique subfolder name based on epoch time and random number
    private String generateUniqueFolderName() {
        long epochTime = Instant.now().toEpochMilli();
        int randomSequence = new Random().nextInt(1000); // Random number between 0 and 999
        return epochTime + "_" + randomSequence;
    }

    // Download Street View images with specified headings and save them locally in a unique subfolder
    public List<String> downloadStreetViewImages(String location, double latitude, double longitude) throws IOException {
        List<String> filePaths = new ArrayList<>();
        int[] headings = {0, 60, 120};  // Different headings for different views

        // Create unique subfolder path
        String uniqueFolderName = generateUniqueFolderName();
        Path downloadPath = Paths.get(downloadFolderPath, uniqueFolderName);

        // Ensure the unique subfolder directory exists
        Files.createDirectories(downloadPath);

        for (int heading : headings) {
            URI uri = buildSVUri(location, heading);
            byte[] imageBytes = restTemplate.getForObject(uri, byte[].class);  // Fetch the image as byte array

            // Save image to a file in the unique subfolder
            String fileName = "street_view_" + heading + ".jpg";
            Path filePath = downloadPath.resolve(fileName);
            try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                outputStream.write(imageBytes);
            }

            filePaths.add(filePath.toString());  // Store the file path
        }

        // Generate the HTML file by calling PanoramicViewService
        panoramicViewService.createHtmlFile(downloadPath, latitude, longitude);

        return filePaths;
    }
}