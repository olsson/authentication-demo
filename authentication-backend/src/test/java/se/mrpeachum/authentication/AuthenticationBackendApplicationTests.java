package se.mrpeachum.authentication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import se.mrpeachum.authentication.auditing.AuditDto;
import se.mrpeachum.authentication.registration.UserDto;

import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationBackendApplicationTests {

    private static final String CLIENT_ID = "authentication_client_1";
    private static final String TEST_PASSWORD = "clever-password";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_cannotAccessSecureResource() {
        ResponseEntity<String> userDetailsResponseEntity =
            restTemplate.getForEntity("/secure/user/tester/login-history", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, userDetailsResponseEntity.getStatusCode());
    }

    @Test
    public void test_registerExistingUser() throws Exception {
        final String userName = UUID.randomUUID().toString();

        // create a new user
        UserDto userDto = new UserDto(userName, TEST_PASSWORD);
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity("/registration/user",
            userDto, Void.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        // try create the same user again
        ResponseEntity<Void> response = restTemplate.postForEntity("/registration/user", userDto, Void.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void test_badPasswordTooShort() throws Exception {
        final String userName = UUID.randomUUID().toString();

        // create a new user
        UserDto userDto = new UserDto(userName, "short");
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity("/registration/user",
            userDto, Void.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void test_badPasswordEmpty() throws Exception {
        final String userName = UUID.randomUUID().toString();

        // create a new user
        UserDto userDto = new UserDto(userName, "");
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity("/registration/user",
            userDto, Void.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void test_badUsernameEmpty() throws Exception {
        // create a new user
        UserDto userDto = new UserDto("", TEST_PASSWORD);
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity("/registration/user",
            userDto, Void.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void test_badUsernameWhitespace() throws Exception {
        // create a new user
        UserDto userDto = new UserDto("       ", TEST_PASSWORD);
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity("/registration/user",
            userDto, Void.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void test_badUsernameTooLong() throws Exception {
        final String userName = UUID.randomUUID().toString();

        // create a new user
        UserDto userDto = new UserDto(userName + userName + userName, TEST_PASSWORD);
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity("/registration/user",
            userDto, Void.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void test_registerAndLogin() throws Exception {
        final String userName = UUID.randomUUID().toString();

        // create a new user
        UserDto userDto = new UserDto(userName, TEST_PASSWORD);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("/registration/user",
            userDto, String.class);

        URI location = responseEntity.getHeaders().getLocation();
        assertEquals(URI.create("/secure/user/" + userName), location);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        // login as the new user
        ResponseEntity<SimpleAccessTokenDto> tokenResponse =
            restTemplate.withBasicAuth(CLIENT_ID, "1234")
                        .postForEntity(
                            "/oauth/token?grant_type=password&username={username}&password={password}&client_id={client_id}",
                            null, SimpleAccessTokenDto.class, userName, TEST_PASSWORD, CLIENT_ID);

        assertNotNull(tokenResponse.getBody().getAccessToken());

        // access a resource with the bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponse.getBody().getAccessToken());
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<AuditDto[]> secureResponse =
            restTemplate.exchange("/secure/user/{tester}/login-history", HttpMethod.GET, requestEntity,
                AuditDto[].class, userName);

        assertEquals("the size of the audit trail should be 1", 1, Arrays.asList(secureResponse.getBody()).size());

    }
}
