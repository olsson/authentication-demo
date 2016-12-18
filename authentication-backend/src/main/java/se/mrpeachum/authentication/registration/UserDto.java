package se.mrpeachum.authentication.registration;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;

@Data
public class UserDto {

    @NotBlank
    @Size(min = 1, max = 50)
    private final String username;

    @Size(min = 8)
    private final String password;
}
