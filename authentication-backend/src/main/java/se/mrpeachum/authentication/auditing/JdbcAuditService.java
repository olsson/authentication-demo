package se.mrpeachum.authentication.auditing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;

@Service
public class JdbcAuditService implements AuditService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String INSERT_AUDIT_EVENT =
        "INSERT INTO audit(username, event_type, client_id, grant_type) " +
            "VALUES(:username, :event_type, :client_id, :grant_type)";

    private static final String GET_AUDIT_EVENTS = "SELECT event_id, event_type, username, client_id, " +
        "grant_type, event_timestamp FROM audit WHERE username = :username AND event_type = :event_type " +
        "ORDER BY event_timestamp DESC LIMIT 5";

    @Autowired
    public JdbcAuditService(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void addAuditEvent(String username, AuditType auditType, String clientId, String grantType) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("username", username)
            .addValue("event_type", auditType.name())
            .addValue("client_id", clientId)
            .addValue("grant_type", grantType);
        jdbcTemplate.update(INSERT_AUDIT_EVENT, params);
    }

    @Override
    public List<AuditDto> getEventsForUser(String userId, AuditType auditType) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("username", userId)
            .addValue("event_type", auditType.name());

        return jdbcTemplate.query(GET_AUDIT_EVENTS, params, (rs, rowNum) ->
            new AuditDto(rs.getInt("event_id"),
                rs.getString("username"),
                AuditType.valueOf(rs.getString("event_type")),
                rs.getString("client_id"),
                rs.getString("grant_type"),
                rs.getTimestamp("event_timestamp").toLocalDateTime())
        );
    }

}
