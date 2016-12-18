package se.mrpeachum.authentication.auditing;

import java.util.List;

public interface AuditService {

    void addAuditEvent(String username, AuditType auditType, String clientId, String grantType);

    List<AuditDto> getEventsForUser(String userId, AuditType loggedIn);
}
