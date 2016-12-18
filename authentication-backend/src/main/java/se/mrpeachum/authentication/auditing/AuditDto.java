package se.mrpeachum.authentication.auditing;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditDto {
    private final Integer eventId;
    private final String username;
    private final AuditType auditType;
    private final String clientId;
    private final String grantType;
    private final LocalDateTime eventTimestamp;
}
