package com.marketnest.ecommerce.service.auth;

import com.marketnest.ecommerce.dto.auth.LoginHistoryDto;
import com.marketnest.ecommerce.mapper.auth.LoginHistoryMapper;
import com.marketnest.ecommerce.model.LoginHistory;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.LoginHistoryRepository;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.email.EmailServiceImpl;
import com.marketnest.ecommerce.service.email.EmailTemplateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {
    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;
    private final GeoLocationService geoLocationService;
    private final EmailServiceImpl emailService;
    private final EmailTemplateService emailTemplateService;
    private final LoginHistoryMapper loginHistoryMapper; // Add this

    public void recordSuccessfulLogin(String email, HttpServletRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LoginHistory loginHistory =
                createLoginHistoryEntry(user, request, LoginHistory.LoginStatus.SUCCESS);

        boolean isSuspicious = detectSuspiciousActivity(user, loginHistory.getIpAddress(),
                loginHistory.getDeviceInfo(), loginHistory.getLocation());
        loginHistory.setSuspicious(isSuspicious);

        loginHistoryRepository.save(loginHistory);

        if (isSuspicious) {
            sendSuspiciousLoginNotification(user, loginHistory);
        } else {
            sendSuccessfulLoginNotification(user, loginHistory);
        }
    }

    private void sendSuspiciousLoginNotification(User user, LoginHistory loginHistory) {
        String subject = "Security Alert: Suspicious Login Detected";
        String body = emailTemplateService.buildSecurityAlertEmailBody(
                user.getFirstName() + " " + user.getLastName(),
                loginHistory.getLoginTimestamp().toString(),
                loginHistory.getIpAddress(),
                loginHistory.getLocation(),
                loginHistory.getDeviceInfo()
        );

        emailService.sendEmail(user.getEmail(), subject, body);
    }

    private void sendSuccessfulLoginNotification(User user, LoginHistory loginHistory) {
        String subject = "Security Alert: Successful Login to Your Account";
        String body = emailTemplateService.buildSuccessfulLoginEmailBody(
                user.getFirstName() + " " + user.getLastName(),
                loginHistory.getLoginTimestamp().toString(),
                loginHistory.getIpAddress(),
                loginHistory.getLocation(),
                loginHistory.getDeviceInfo()
        );

        emailService.sendEmail(user.getEmail(), subject, body);
    }

    public void recordFailedLogin(String email, HttpServletRequest request) {
        User user = userRepository.findByEmail(email).orElse(null);

        LoginHistory loginHistory =
                createLoginHistoryEntry(user, request, LoginHistory.LoginStatus.FAILED);
        loginHistoryRepository.save(loginHistory);

        if (user != null) {
            checkBruteForceAttempts(user, loginHistory.getIpAddress());
        }
    }

    private boolean detectSuspiciousActivity(User user, String ipAddress, String deviceInfo,
                                             String location) {
        Optional<LoginHistory> lastLoginOpt = loginHistoryRepository
                .findTopByUserAndStatusOrderByLoginTimestampDesc(user,
                        LoginHistory.LoginStatus.SUCCESS);

        if (lastLoginOpt.isEmpty()) {
            return false;
        }

        LoginHistory lastLogin = lastLoginOpt.get();

        return !lastLogin.getIpAddress().equals(ipAddress) ||
               !lastLogin.getLocation().equals(location) ||
               !lastLogin.getDeviceInfo().equals(deviceInfo);
    }

    private void checkBruteForceAttempts(User user, String ipAddress) {
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        List<LoginHistory> recentFailures = loginHistoryRepository
                .findByUserAndStatusAndLoginTimestampAfter(
                        user, LoginHistory.LoginStatus.FAILED, oneHourAgo);

        if (recentFailures.size() >= 5) {
            String subject = "Security Alert: Multiple Failed Login Attempts";

            String body = emailTemplateService.buildBruteForceAlertEmailBody(
                    user.getFirstName() + " " + user.getLastName(),
                    recentFailures.size(),
                    ipAddress
            );

            emailService.sendEmail(user.getEmail(), subject, body);
        }
    }

    private String extractIpAddress(HttpServletRequest request) {
        String[] headersToCheck = {
                "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR", "HTTP_CLIENT_IP"
        };

        for (String header : headersToCheck) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip;
            }
        }

        return request.getRemoteAddr();
    }

    public List<LoginHistoryDto> getUserLoginHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<LoginHistory> history =
                loginHistoryRepository.findByUserOrderByLoginTimestampDesc(user);
        return loginHistoryMapper.toDtoList(history);
    }

    private LoginHistory createLoginHistoryEntry(User user, HttpServletRequest request,
                                                 LoginHistory.LoginStatus status) {
        String ipAddress = extractIpAddress(request);
        String deviceInfo = request.getHeader("User-Agent");
        String location = geoLocationService.getLocationFromIp(ipAddress);

        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setUser(user);
        loginHistory.setLoginTimestamp(Instant.now());
        loginHistory.setIpAddress(ipAddress);
        loginHistory.setDeviceInfo(deviceInfo);
        loginHistory.setLocation(location);
        loginHistory.setStatus(status);

        return loginHistory;
    }

}