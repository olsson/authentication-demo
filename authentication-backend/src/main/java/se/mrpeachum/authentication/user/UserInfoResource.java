package se.mrpeachum.authentication.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.mrpeachum.authentication.auditing.AuditDto;
import se.mrpeachum.authentication.auditing.AuditService;
import se.mrpeachum.authentication.auditing.AuditType;

import java.util.List;

@RestController
@RequestMapping("secure/user")
public class UserInfoResource {

    private final UserDetailsService userDetailsService;
    private final AuditService auditService;

    @Autowired
    public UserInfoResource(UserDetailsService userDetailsService, AuditService auditService) {
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
    }

    @RequestMapping("{userId}")
    public UserDetails getUserDetails(@PathVariable String userId) {
        return userDetailsService.loadUserByUsername(userId);
    }

    @RequestMapping("{userId}/login-history")
    public List<AuditDto> getLogins(@PathVariable String userId) {
        return auditService.getEventsForUser(userId, AuditType.LOGGED_IN);
    }
}
