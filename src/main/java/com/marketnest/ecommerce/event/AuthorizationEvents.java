package com.marketnest.ecommerce.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthorizationEvents {

    @EventListener
    public void onFail(AuthorizationDeniedEvent<?> deniedEvent) {
        log.error("Authorization failed for the user: {} due to: {}",
                deniedEvent.getAuthentication().get().getName(),
                deniedEvent.getAuthorizationDecision().toString());
    }
}
