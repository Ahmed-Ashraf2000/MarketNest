package com.marketnest.ecommerce.event;

import com.marketnest.ecommerce.service.auth.LoginHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationEvents {
    private final LoginHistoryService loginHistoryService;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent successEvent) {
        String email = successEvent.getAuthentication().getName();
        HttpServletRequest request = getCurrentRequest();

        if (request != null) {
            loginHistoryService.recordSuccessfulLogin(email, request);
        } else {
            log.warn("Could not get request details for login notification for user with email: {}",
                    email);
        }
    }

    @EventListener
    public void onFail(AbstractAuthenticationFailureEvent failureEvent) {
        String email = failureEvent.getAuthentication().getName();
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            loginHistoryService.recordFailedLogin(email, request);
        }
        log.error("Failed login attempt by user with email: {} due to: {}",
                failureEvent.getAuthentication().getName(),
                failureEvent.getException().getMessage());
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            return ((ServletRequestAttributes) Objects.requireNonNull(
                    RequestContextHolder.getRequestAttributes())).getRequest();
        } catch (Exception e) {
            return null;
        }
    }
}
