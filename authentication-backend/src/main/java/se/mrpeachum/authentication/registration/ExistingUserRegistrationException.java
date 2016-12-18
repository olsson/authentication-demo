package se.mrpeachum.authentication.registration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
class ExistingUserRegistrationException extends RuntimeException {

    ExistingUserRegistrationException(String userName) {
        super(String.format("The username %s is already being used", userName));
    }

}
