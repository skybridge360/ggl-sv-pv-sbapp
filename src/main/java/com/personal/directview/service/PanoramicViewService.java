package com.personal.directview.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

@Service
public class PanoramicViewService {

    @Value("${google.api.key}")
    private String apiKey;

    // Create the HTML file for Google Maps Street View with specified latitude and longitude
    public void createHtmlFile(Path directory, double latitude, double longitude) throws IOException {
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>Google Maps Street View</title>\n" +
                "    <style>\n" +
                "      /* Set the size for the map container */\n" +
                "      #street-view {\n" +
                "        height: 800px;\n" +
                "        width: 100%;\n" +
                "      }\n" +
                "    </style>\n" +
                "    <script src=\"https://maps.googleapis.com/maps/api/js?key=" + apiKey + "\"></script>\n" +
                "    <script>\n" +
                "      // Initialize and display the Street View panorama\n" +
                "      function initializeStreetView() {\n" +
                "        var lat = " + latitude + ";\n" +
                "        var lng = " + longitude + ";\n" +
                "\n" +
                "        // Create a LatLng object\n" +
                "        var location = { lat: lat, lng: lng };\n" +
                "        \n" +
                "        // Initialize the Street View panorama\n" +
                "        var panorama = new google.maps.StreetViewPanorama(\n" +
                "          document.getElementById('street-view'), {\n" +
                "            position: location,        // Set the position to the passed lat-lng\n" +
                "            pov: {                     // POV is the point of view\n" +
                "              heading: 165,            // Initial camera heading (optional)\n" +
                "              pitch: 0                 // Initial camera pitch (optional)\n" +
                "            },\n" +
                "            zoom: 1                    // Set initial zoom level (optional)\n" +
                "          }\n" +
                "        );\n" +
                "      }\n" +
                "\n" +
                "      // Run the initialize function when the window loads\n" +
                "      window.onload = initializeStreetView;\n" +
                "    </script>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <h1>Draggable Street View</h1>\n" +
                "    <!-- The container for the street view -->\n" +
                "    <div id=\"street-view\"></div>\n" +
                "  </body>\n" +
                "</html>";

        // Write the HTML content to a file
        Path htmlFilePath = directory.resolve("street_view.html");
        try (FileWriter writer = new FileWriter(htmlFilePath.toFile())) {
            writer.write(htmlContent);
        }
    }
}