package se.mrpeachum.authentication.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("registration")
public class RegistrationResource {

    private final UserDetailsManager userDetailsManager;
    private final UserDetailsMapper userDetailsMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationResource(UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
        userDetailsMapper = new UserDetailsMapper();
    }

    @RequestMapping(value = "user", method = RequestMethod.POST)
    public ResponseEntity registerUser(@RequestBody @Valid UserDto userDto) {

        if (userDetailsManager.userExists(userDto.getUsername())) {
            throw new ExistingUserRegistrationException(userDto.getUsername());
        }

        UserDetails userDetails = userDetailsMapper.map(userDto, passwordEncoder);
        userDetailsManager.createUser(userDetails);
        return ResponseEntity.created(URI.create("/secure/user/" + userDetails.getUsername())).build();
    }
}
