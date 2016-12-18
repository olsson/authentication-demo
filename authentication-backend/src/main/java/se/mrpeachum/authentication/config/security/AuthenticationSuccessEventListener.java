package se.mrpeachum.authentication.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;
import se.mrpeachum.authentication.auditing.AuditService;
import se.mrpeachum.authentication.auditing.AuditType;

import java.util.Map;

@Component
public class AuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final AuditService auditService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationSuccessEventListener.class);

    @Autowired
    public AuthenticationSuccessEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        LOGGER.debug("Got event: {}", event);
        try {
            if (event.getAuthentication().getDetails() instanceof OAuth2AuthenticationDetails) {
                LOGGER.debug("Resource access by bearer: {}", event);
            } else {
                Map<String, String> details = (Map<String, String>) event.getAuthentication().getDetails();
                auditService.addAuditEvent(details.get("username"), AuditType.LOGGED_IN, details.get("client_id"), details.get("grant_type"));
            }
        } catch (Exception ex) {
            LOGGER.warn("Unable to save audit event {}, due to exception: {}", event, ex);
        }
    }
}
