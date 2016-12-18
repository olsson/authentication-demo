package se.mrpeachum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties("cli.oauth2")
public class ClientProperties {
    private String accessUri;
    private String clientId;
    private String clientSecret;
    private String grantType;
}
