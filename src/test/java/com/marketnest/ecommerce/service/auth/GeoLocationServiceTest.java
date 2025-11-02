package com.marketnest.ecommerce.service.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeoLocationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GeoLocationService geoLocationService;

    @Test
    void getLocationFromIp_shouldReturnLocal_whenIpIsLocalhost() {
        String result = geoLocationService.getLocationFromIp("127.0.0.1");

        assertThat(result).isEqualTo("Local");
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void getLocationFromIp_shouldReturnLocal_whenIpIsIPv6Localhost() {
        String result = geoLocationService.getLocationFromIp("0:0:0:0:0:0:0:1");

        assertThat(result).isEqualTo("Local");
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void getLocationFromIp_shouldReturnCityCountry_whenValidIpAndSuccessfulResponse() {
        String testIp = "8.8.8.8";
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");
        mockResponse.put("city", "Cairo");
        mockResponse.put("country", "Egypt");

        when(restTemplate.getForObject(eq("http://ip-api.com/json/" + testIp), eq(Map.class)))
                .thenReturn(mockResponse);

        String result = geoLocationService.getLocationFromIp(testIp);

        assertThat(result).isEqualTo("Cairo, Egypt");
        verify(restTemplate).getForObject(eq("http://ip-api.com/json/" + testIp), eq(Map.class));
    }

    @Test
    void getLocationFromIp_shouldReturnUnknown_whenApiReturnsFailStatus() {
        String testIp = "8.8.8.8";
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "fail");

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(mockResponse);

        String result = geoLocationService.getLocationFromIp(testIp);

        assertThat(result).isEqualTo("Unknown");
    }

    @Test
    void getLocationFromIp_shouldReturnUnknown_whenResponseIsNull() {
        String testIp = "8.8.8.8";

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(null);

        String result = geoLocationService.getLocationFromIp(testIp);

        assertThat(result).isEqualTo("Unknown");
    }

    @Test
    void getLocationFromIp_shouldReturnUnknown_whenRestTemplateThrowsException() {
        String testIp = "8.8.8.8";

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RestClientException("Network error"));

        String result = geoLocationService.getLocationFromIp(testIp);

        assertThat(result).isEqualTo("Unknown");
    }

    @Test
    void getLocationFromIp_shouldReturnUnknown_whenResponseMissingCityOrCountry() {
        String testIp = "8.8.8.8";
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");
        mockResponse.put("city", null);
        mockResponse.put("country", null);

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(mockResponse);

        String result = geoLocationService.getLocationFromIp(testIp);

        assertThat(result).isEqualTo("null, null");
    }

    @Test
    void getLocationFromIp_shouldHandleInternationalCharacters() {
        String testIp = "1.2.3.4";
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");
        mockResponse.put("city", "Giza");
        mockResponse.put("country", "Egypt");

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(mockResponse);

        String result = geoLocationService.getLocationFromIp(testIp);

        assertThat(result).isEqualTo("Giza, Egypt");
    }
}