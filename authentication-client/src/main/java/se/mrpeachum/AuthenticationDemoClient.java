package se.mrpeachum;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import se.mrpeachum.config.ClientProperties;
import se.mrpeachum.dto.SimpleAccessTokenDto;
import se.mrpeachum.dto.UserDto;

import java.io.Console;
import java.util.*;

@SpringBootApplication
public class AuthenticationDemoClient implements ApplicationRunner {

    private final ApplicationContext applicationContext;
    private final ClientProperties clientProperties;
    private final RestOperations restOperations;
    private final RestOperations authenticatedRestOperations;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    public AuthenticationDemoClient(ApplicationContext applicationContext, ClientProperties clientProperties) {
        this.applicationContext = applicationContext;
        this.clientProperties = clientProperties;
        restOperations = new RestTemplate();

        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = Collections.<ClientHttpRequestInterceptor>singletonList(
            new BasicAuthorizationInterceptor(clientProperties.getClientId(), clientProperties.getClientSecret()));
        restTemplate.setRequestFactory(new InterceptingClientHttpRequestFactory(
            restTemplate.getRequestFactory(), interceptors));
        this.authenticatedRestOperations = restTemplate;
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(AuthenticationDemoClient.class)
            .web(false)
            .run(args);
    }

    public void run(ApplicationArguments args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(green("\n[1] Register a new user\n" +
                "[2] Login and securely access resources\n" +
                "[3] Access random user resource unauthenticated\n" +
                "[4] Quit\n" +
                "Select an option: "));

            String initialChoice = scanner.nextLine();

            if ("4".equals(initialChoice)) {
                SpringApplication.exit(applicationContext, () -> 0);
                return;
            } else if ("3".equals(initialChoice)) {
                hasToken(null, UUID.randomUUID().toString());
            } else if ("2".equals(initialChoice)) {
                login();
            } else if ("1".equals(initialChoice)) {
                registerUser();
            }
        }
    }


    private void login() throws Exception {
        System.out.println(green("\nLog in as an existing user"));
        CredentialPair credentialPair = requestCredentials();
        userRegistered(credentialPair.username, credentialPair.password);
    }

    private void registerUser() throws Exception {
        System.out.println(green("\nRegister a new user"));
        CredentialPair credentialPair = requestCredentials();
        try {
            ResponseEntity<Void> responseEntity =
                restOperations.postForEntity(clientProperties.getAccessUri() + "/registration/user",
                    new UserDto(credentialPair.username, credentialPair.password), Void.class);
            if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
                System.out.println(String.format("\nResponse: HTTP %d %s\nHeaders: \n%s", responseEntity.getStatusCode()
                                                                                                        .value(),
                    responseEntity.getStatusCode().getReasonPhrase(),
                    Arrays.toString(responseEntity.getHeaders().entrySet().toArray())));
                userRegistered(credentialPair.username, credentialPair.password);
            }
        } catch (RestClientResponseException restException) {
            System.out.println(String.format("\nREST exception: HTTP %d \n%s",
                restException.getRawStatusCode(), asFormattedJson(restException.getResponseBodyAsString())));
        }

    }

    private void userRegistered(String username, String password) throws Exception {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.printf(green("\n[1] Continue to log in as %s\n" +
                "[2] Go Back\n" +
                "Select an option: "), username);

            String choice = scanner.nextLine().trim();

            if ("2".equals(choice)) {
                return;
            } else if ("1".equals(choice)) {

                ResponseEntity<SimpleAccessTokenDto> accessTokenResponseEntity;
                try {
                    accessTokenResponseEntity = authenticatedRestOperations.postForEntity(clientProperties.getAccessUri() +
                            "/oauth/token?grant_type={grantType}&username={username}&password={password}&client_id={clientId}",
                        null, SimpleAccessTokenDto.class, clientProperties.getGrantType(), username, password,
                        clientProperties.getClientId());

                    if (accessTokenResponseEntity.getStatusCode() == HttpStatus.OK) {
                        System.out.println(String.format("\nResponse: HTTP %d %s\nToken: %s",
                            accessTokenResponseEntity.getStatusCode().value(),
                            accessTokenResponseEntity.getStatusCode().getReasonPhrase(),
                            accessTokenResponseEntity.getBody().getAccessToken()));

                        hasToken(accessTokenResponseEntity.getBody(), username);
                    }

                } catch (RestClientResponseException restException) {
                    System.out.println(String.format("\nREST exception: HTTP %d %s",
                        restException.getRawStatusCode(), asFormattedJson(restException.getResponseBodyAsString())));
                }

            }
        }
    }

    private void hasToken(SimpleAccessTokenDto tokenDto, String username) throws Exception {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.printf(green("\n[1] Get protected resource /secure/user/%s\n" +
                "[2] Get protected resource /secure/user/%s/login-history\n" +
                "[3] Go Back\n" +
                "Select an option: "), username, username);

            String choice = scanner.nextLine().trim();

            if ("3".equals(choice)) {
                return;
            } else if ("1".equals(choice)) {
                getResourceWithToken("/secure/user/" + username, tokenDto);
            } else if ("2".equals(choice)) {
                getResourceWithToken("/secure/user/" + username + "/login-history", tokenDto);
            }
        }
    }

    private void getResourceWithToken(String resourcePath, SimpleAccessTokenDto tokenDto) throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            Optional.ofNullable(tokenDto)
                    .ifPresent(simpleAccessTokenDto -> headers.add(HttpHeaders.AUTHORIZATION,
                        "Bearer " + simpleAccessTokenDto.getAccessToken()));
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> responseEntity =
                restOperations.exchange(clientProperties.getAccessUri() + resourcePath, HttpMethod.GET, requestEntity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                System.out.println(String.format("\nResponse: HTTP %d %s\nResponse: \n%s",
                    responseEntity.getStatusCode().value(),
                    responseEntity.getStatusCode().getReasonPhrase(),
                    asFormattedJson(responseEntity.getBody())));
            }
        } catch (RestClientResponseException restException) {
            System.out.println(String.format("\nREST exception: HTTP %d \n%s",
                restException.getRawStatusCode(), asFormattedJson(restException.getResponseBodyAsString())));
        }
    }

    private String asFormattedJson(String inputJson) throws Exception {
        JsonNode bodyObject = OBJECT_MAPPER.readTree(inputJson);
        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(bodyObject);
    }

    private String green(String input) {
        return (char) 27 + "[32m" + input + (char) 27 + "[0m";
    }

    private CredentialPair requestCredentials() {
        Console console = System.console();
        Scanner scanner = new Scanner(System.in);

        System.out.print(green("\nEnter username: "));
        String username = scanner.nextLine().trim();

        String password = new String(console.readPassword(green("Enter password: ")));
        return new CredentialPair(username, password);
    }

    private static class CredentialPair {
        private final String username;
        private final String password;

        private CredentialPair(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
