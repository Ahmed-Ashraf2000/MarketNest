package com.marketnest.ecommerce.utils;

import jakarta.servlet.http.HttpServletRequest;

public class AuthUtils {

    public static String buildBaseUrl(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName();
        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
            baseUrl += ":" + request.getServerPort();
        }
        return baseUrl;
    }
}
