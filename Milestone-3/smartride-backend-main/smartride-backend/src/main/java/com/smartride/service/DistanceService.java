package com.smartride.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * DistanceService - Uses an external routing API (OSRM) to calculate distance between two points.
 */
@Service
public class DistanceService {

    private static final String OSRM_URL = "http://router.project-osrm.org/route/v1/driving";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DistanceService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Calls the OSRM route API and returns the distance in kilometers.
     *
     * @param originLat      latitude of origin
     * @param originLng      longitude of origin
     * @param destinationLat latitude of destination
     * @param destinationLng longitude of destination
     * @return distance in kilometers
     */
    public double getDistanceInKm(double originLat, double originLng,
                                  double destinationLat, double destinationLng) {

        String coordinates = String.format("%f,%f;%f,%f", originLng, originLat, destinationLng, destinationLat);

        URI uri = UriComponentsBuilder.fromHttpUrl(OSRM_URL)
                .pathSegment(coordinates)
                .queryParam("overview", "false")
                .build(true)
                .toUri();

        String json = restTemplate.getForObject(uri, String.class);
        if (json == null) {
            throw new RuntimeException("OSRM returned an empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.path("code").asText().equalsIgnoreCase("Ok")) {
                throw new RuntimeException("OSRM returned error: " + root.path("message").asText());
            }

            JsonNode routes = root.path("routes");
            if (!routes.isArray() || routes.isEmpty()) {
                throw new RuntimeException("No routes returned from OSRM");
            }

            double distanceMeters = routes.get(0).path("distance").asDouble(0);
            return distanceMeters / 1000.0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OSRM response", e);
        }
    }
}
