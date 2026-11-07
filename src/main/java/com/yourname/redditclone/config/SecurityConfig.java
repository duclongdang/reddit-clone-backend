package com.yourname.redditclone.config;

import com.yourname.redditclone.auth.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final com.yourname.redditclone.auth.CustomOAuth2UserService customOAuth2UserService;
    private final com.yourname.redditclone.auth.CustomOidcUserService customOidcUserService;
    private final com.yourname.redditclone.auth.OAuth2AuthenticationSuccessHandler successHandler;
    private final com.yourname.redditclone.auth.OAuth2AuthenticationFailureHandler failureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(com.yourname.redditclone.auth.CustomOAuth2UserService customOAuth2UserService,
                          com.yourname.redditclone.auth.CustomOidcUserService customOidcUserService,
                          com.yourname.redditclone.auth.OAuth2AuthenticationSuccessHandler successHandler,
                          com.yourname.redditclone.auth.OAuth2AuthenticationFailureHandler failureHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOidcUserService = customOidcUserService;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health").permitAll()
                        .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                                .oidcUserService(customOidcUserService)
                        )
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
