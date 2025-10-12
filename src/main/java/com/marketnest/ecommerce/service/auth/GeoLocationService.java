package com.marketnest.ecommerce.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeoLocationService {
    private final RestTemplate restTemplate;

    public String getLocationFromIp(String ipAddress) {
        try {
            if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
                return "Local";
            }

            String url = "http://ip-api.com/json/" + ipAddress;
            var response = restTemplate.getForObject(url, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                String city = (String) response.get("city");
                String country = (String) response.get("country");
                return city + ", " + country;
            }
            return "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }
}