package se.mrpeachum.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class SimpleAccessTokenDto {
    @JsonProperty("access_token")
    private final String accessToken;
}
