package se.mrpeachum.authentication.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.security.KeyPair;

@Configuration
public class OAuth2Configuration {

    private static final String PUBLIC_KEY = "public_key.der";
    private static final String PRIVATE_KEY = "private_key.der";

    private static final KeyPair KEY_PAIR = KeyUtils.getKeyPair(PUBLIC_KEY, PRIVATE_KEY);

    @Configuration
    @EnableResourceServer
    protected static class ResourceConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.exceptionHandling()
                .and()
                .logout()
                    .logoutUrl("/oauth/logout")
                    .and()
                .csrf()
                    .requireCsrfProtectionMatcher(new AntPathRequestMatcher("/oauth/token"))
                    .and()
                .headers()
                    .frameOptions().deny()
                    .xssProtection().xssProtectionEnabled(true).and()
                    .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                .authorizeRequests()
                    .antMatchers("/registration/**").permitAll()
                    .antMatchers("/secure/**").authenticated();

        }

    }

    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationConfiguration extends AuthorizationServerConfigurerAdapter {

        private final AuthenticationManager authenticationManager;
        private final UserDetailsService userDetailsService;
        private final TokenStore tokenStore;
        private final AccessTokenConverter accessTokenConverter;

        @Autowired
        public AuthorizationConfiguration(@Qualifier("authenticationManagerBean") AuthenticationManager authenticationManager,
                                          UserDetailsService userDetailsService, TokenStore tokenStore,
                                          @Qualifier("jwtAccessTokenConverter") AccessTokenConverter accessTokenConverter) {
            this.authenticationManager = authenticationManager;
            this.userDetailsService = userDetailsService;
            this.tokenStore = tokenStore;
            this.accessTokenConverter = accessTokenConverter;
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints.tokenStore(tokenStore)
                     .accessTokenConverter(accessTokenConverter)
                     .userDetailsService(userDetailsService)
                     .authenticationManager(authenticationManager);
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.inMemory()
                   .withClient("authentication_client_1")
                   .secret("1234")
                   .scopes("read", "write")
                   .authorizedGrantTypes("password", "refresh_token")
                   .accessTokenValiditySeconds(3600)
                   .refreshTokenValiditySeconds(36000);
        }
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(KEY_PAIR);
        return converter;
    }

}
