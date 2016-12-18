package se.mrpeachum.authentication.registration;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

class UserDetailsMapper {

    UserDetails map(UserDto userDto, PasswordEncoder passwordEncoder) {
        return new User(userDto.getUsername(), passwordEncoder.encode(userDto.getPassword()),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
