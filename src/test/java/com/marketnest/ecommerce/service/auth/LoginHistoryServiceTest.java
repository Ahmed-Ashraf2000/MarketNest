package com.marketnest.ecommerce.service.auth;

import com.marketnest.ecommerce.dto.auth.LoginHistoryDto;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.mapper.auth.LoginHistoryMapper;
import com.marketnest.ecommerce.model.LoginHistory;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.LoginHistoryRepository;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.email.EmailServiceImpl;
import com.marketnest.ecommerce.service.email.EmailTemplateService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginHistoryServiceTest {

    @Mock
    private LoginHistoryRepository loginHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GeoLocationService geoLocationService;

    @Mock
    private EmailServiceImpl emailService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @Mock
    private LoginHistoryMapper loginHistoryMapper;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private LoginHistoryService loginHistoryService;

    private User testUser;
    private LoginHistory testLoginHistory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Ahmed");
        testUser.setLastName("Ashraf");

        testLoginHistory = new LoginHistory();
        testLoginHistory.setUser(testUser);
        testLoginHistory.setIpAddress("192.168.1.1");
        testLoginHistory.setDeviceInfo("Chrome/Linux");
        testLoginHistory.setLocation("Cairo, Egypt");
        testLoginHistory.setLoginTimestamp(Instant.now());
        testLoginHistory.setStatus(LoginHistory.LoginStatus.SUCCESS);
        testLoginHistory.setSuspicious(false);
    }

    @Test
    void recordSuccessfulLogin_shouldSaveLoginHistory_whenUserExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Chrome/Linux");
        when(geoLocationService.getLocationFromIp("192.168.1.1")).thenReturn("Cairo, Egypt");
        when(loginHistoryRepository.findTopByUserAndStatusOrderByLoginTimestampDesc(
                testUser, LoginHistory.LoginStatus.SUCCESS))
                .thenReturn(Optional.empty());
        when(emailTemplateService.buildSuccessfulLoginEmailBody(anyString(), anyString(),
                anyString(), anyString(), anyString()))
                .thenReturn("Email body");

        loginHistoryService.recordSuccessfulLogin("test@example.com", request);

        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository).save(captor.capture());

        LoginHistory saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(testUser);
        assertThat(saved.getStatus()).isEqualTo(LoginHistory.LoginStatus.SUCCESS);
        assertThat(saved.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(saved.getDeviceInfo()).isEqualTo("Chrome/Linux");
        assertThat(saved.getLocation()).isEqualTo("Cairo, Egypt");
        assertThat(saved.isSuspicious()).isFalse();

        verify(emailService).sendEmail(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    void recordSuccessfulLogin_shouldMarkAsSuspicious_whenDifferentLocation() {
        LoginHistory lastLogin = new LoginHistory();
        lastLogin.setIpAddress("10.0.0.1");
        lastLogin.setLocation("Los Angeles, Egypt");
        lastLogin.setDeviceInfo("Chrome/Linux");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Chrome/Linux");
        when(geoLocationService.getLocationFromIp("192.168.1.1")).thenReturn("Cairo, Egypt");
        when(loginHistoryRepository.findTopByUserAndStatusOrderByLoginTimestampDesc(
                testUser, LoginHistory.LoginStatus.SUCCESS))
                .thenReturn(Optional.of(lastLogin));
        when(emailTemplateService.buildSecurityAlertEmailBody(anyString(), anyString(),
                anyString(), anyString(), anyString()))
                .thenReturn("Security alert body");

        loginHistoryService.recordSuccessfulLogin("test@example.com", request);

        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository).save(captor.capture());

        LoginHistory saved = captor.getValue();
        assertThat(saved.isSuspicious()).isTrue();

        verify(emailService).sendEmail(eq("test@example.com"),
                eq("Security Alert: Suspicious Login Detected"), anyString());
    }

    @Test
    void recordSuccessfulLogin_shouldMarkAsSuspicious_whenDifferentDevice() {
        LoginHistory lastLogin = new LoginHistory();
        lastLogin.setIpAddress("192.168.1.1");
        lastLogin.setLocation("Cairo, Egypt");
        lastLogin.setDeviceInfo("Safari/MacOS");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Chrome/Linux");
        when(geoLocationService.getLocationFromIp("192.168.1.1")).thenReturn("Cairo, Egypt");
        when(loginHistoryRepository.findTopByUserAndStatusOrderByLoginTimestampDesc(
                testUser, LoginHistory.LoginStatus.SUCCESS))
                .thenReturn(Optional.of(lastLogin));
        when(emailTemplateService.buildSecurityAlertEmailBody(anyString(), anyString(),
                anyString(), anyString(), anyString()))
                .thenReturn("Security alert body");

        loginHistoryService.recordSuccessfulLogin("test@example.com", request);

        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository).save(captor.capture());

        assertThat(captor.getValue().isSuspicious()).isTrue();
    }

    @Test
    void recordSuccessfulLogin_shouldExtractIpFromXForwardedFor() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Chrome/Linux");
        when(geoLocationService.getLocationFromIp("203.0.113.1")).thenReturn("Cairo, Egypt");
        when(loginHistoryRepository.findTopByUserAndStatusOrderByLoginTimestampDesc(
                any(), any())).thenReturn(Optional.empty());
        when(emailTemplateService.buildSuccessfulLoginEmailBody(anyString(), anyString(),
                anyString(), anyString(), anyString()))
                .thenReturn("Email body");

        loginHistoryService.recordSuccessfulLogin("test@example.com", request);

        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository).save(captor.capture());

        assertThat(captor.getValue().getIpAddress()).isEqualTo("203.0.113.1");
    }

    @Test
    void recordFailedLogin_shouldSaveFailedLoginHistory() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Chrome/Linux");
        when(geoLocationService.getLocationFromIp("192.168.1.1")).thenReturn("Cairo, Egypt");
        when(loginHistoryRepository.findByUserAndStatusAndLoginTimestampAfter(
                any(), eq(LoginHistory.LoginStatus.FAILED), any()))
                .thenReturn(new ArrayList<>());

        loginHistoryService.recordFailedLogin("test@example.com", request);

        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository).save(captor.capture());

        LoginHistory saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(LoginHistory.LoginStatus.FAILED);
        assertThat(saved.getUser()).isEqualTo(testUser);
    }

    @Test
    void recordFailedLogin_shouldSendBruteForceAlert_whenMultipleFailures() {
        List<LoginHistory> recentFailures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LoginHistory failure = new LoginHistory();
            failure.setStatus(LoginHistory.LoginStatus.FAILED);
            recentFailures.add(failure);
        }

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Chrome/Linux");
        when(geoLocationService.getLocationFromIp("192.168.1.1")).thenReturn("Cairo, Egypt");
        when(loginHistoryRepository.findByUserAndStatusAndLoginTimestampAfter(
                eq(testUser), eq(LoginHistory.LoginStatus.FAILED), any(Instant.class)))
                .thenReturn(recentFailures);
        when(emailTemplateService.buildBruteForceAlertEmailBody(anyString(), anyInt(), anyString()))
                .thenReturn("Brute force alert");

        loginHistoryService.recordFailedLogin("test@example.com", request);

        verify(emailService).sendEmail(eq("test@example.com"),
                eq("Security Alert: Multiple Failed Login Attempts"), anyString());
    }

    @Test
    void recordFailedLogin_shouldHandleUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Chrome/Linux");
        when(geoLocationService.getLocationFromIp("192.168.1.1")).thenReturn("Unknown");

        loginHistoryService.recordFailedLogin("unknown@example.com", request);

        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository).save(captor.capture());

        assertThat(captor.getValue().getUser()).isNull();
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void getUserLoginHistory_shouldReturnLoginHistory_whenUserExists() {
        LoginHistoryDto dto = new LoginHistoryDto();
        dto.setIpAddress("192.168.1.1");
        dto.setLocation("Cairo, Egypt");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loginHistoryRepository.findByUserOrderByLoginTimestampDesc(testUser))
                .thenReturn(Collections.singletonList(testLoginHistory));
        when(loginHistoryMapper.toDtoList(anyList())).thenReturn(List.of(dto));

        List<LoginHistoryDto> result = loginHistoryService.getUserLoginHistory("test@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getIpAddress()).isEqualTo("192.168.1.1");
        verify(loginHistoryRepository).findByUserOrderByLoginTimestampDesc(testUser);
    }

    @Test
    void getUserLoginHistory_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginHistoryService.getUserLoginHistory("unknown@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(loginHistoryRepository, never()).findByUserOrderByLoginTimestampDesc(any());
    }

    @Test
    void recordSuccessfulLogin_shouldNotMarkSuspicious_whenFirstLogin() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Chrome/Linux");
        when(geoLocationService.getLocationFromIp("192.168.1.1")).thenReturn("Cairo, Egypt");
        when(loginHistoryRepository.findTopByUserAndStatusOrderByLoginTimestampDesc(
                testUser, LoginHistory.LoginStatus.SUCCESS))
                .thenReturn(Optional.empty());
        when(emailTemplateService.buildSuccessfulLoginEmailBody(anyString(), anyString(),
                anyString(), anyString(), anyString()))
                .thenReturn("Email body");

        loginHistoryService.recordSuccessfulLogin("test@example.com", request);

        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository).save(captor.capture());

        assertThat(captor.getValue().isSuspicious()).isFalse();
    }

    @Test
    void recordFailedLogin_shouldNotSendAlert_whenLessThanFiveFailures() {
        List<LoginHistory> recentFailures = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            LoginHistory failure = new LoginHistory();
            recentFailures.add(failure);
        }

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Chrome/Linux");
        when(geoLocationService.getLocationFromIp("192.168.1.1")).thenReturn("Cairo, Egypt");
        when(loginHistoryRepository.findByUserAndStatusAndLoginTimestampAfter(
                any(), any(), any())).thenReturn(recentFailures);

        loginHistoryService.recordFailedLogin("test@example.com", request);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}