package com.marketnest.ecommerce.config;

import com.marketnest.ecommerce.exception.SecurityExceptionHandlers;
import com.marketnest.ecommerce.filter.CsrfCookieFilter;
import com.marketnest.ecommerce.filter.JwtTokenGeneratorFilter;
import com.marketnest.ecommerce.filter.JwtTokenValidatorFilter;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.security.ApplicationUsernamePwdAuthenticationProvider;
import com.marketnest.ecommerce.service.auth.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtService jwtService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler =
                new CsrfTokenRequestAttributeHandler();

        http.authorizeHttpRequests(
                authorize -> authorize.
                        requestMatchers(HttpMethod.POST, "/api/auth/register",
                                "/api/auth/resend-token", "/api/auth/login",
                                "/api/auth/refresh-token", "/api/auth/forgot-password",
                                "/api/auth/reset-password").permitAll().
                        requestMatchers(HttpMethod.GET, "/api/auth/verify-email").permitAll().
                        requestMatchers(HttpMethod.GET, "/api/auth/login-history")
                        .hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/auth/change-password")
                        .hasAnyRole("CUSTOMER", "ADMIN").
                        requestMatchers(HttpMethod.POST, "/api/auth/logout")
                        .authenticated().
                        requestMatchers(HttpMethod.POST, "/api/users/addresses")
                        .hasRole("CUSTOMER").
                        requestMatchers(HttpMethod.GET, "/api/users/addresses/**")
                        .hasRole("CUSTOMER").
                        requestMatchers(HttpMethod.PUT, "/api/users/addresses/**")
                        .hasRole("CUSTOMER").
                        requestMatchers(HttpMethod.PATCH, "/api/users/addresses/**")
                        .hasRole("CUSTOMER")
        );

        http.formLogin(Customizer.withDefaults());

        http.httpBasic(basicConfigurer -> basicConfigurer.authenticationEntryPoint(
                new SecurityExceptionHandlers.CustomAuthenticationEntryPoint()));

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http.csrf(csrfConfigurer -> csrfConfigurer.csrfTokenRequestHandler(
                        csrfTokenRequestAttributeHandler)
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/**")
        );

        http.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);

        http.addFilterAfter(new JwtTokenGeneratorFilter(jwtService),
                BasicAuthenticationFilter.class);

        http.addFilterBefore(new JwtTokenValidatorFilter(jwtService),
                BasicAuthenticationFilter.class);

        http.exceptionHandling(exceptionHandling ->
                exceptionHandling.accessDeniedHandler(
                        new SecurityExceptionHandlers.CustomAccessDeniedHandler())
        );

        http.sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.logout(logoutConfigurer -> logoutConfigurer.invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "X-XSRF-TOKEN", "refresh_token")
                .clearAuthentication(true));

        http.headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                        .policyDirectives(
                                "default-src 'self'; script-src 'self'; object-src 'none'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; frame-ancestors 'none'"))
                .xssProtection(HeadersConfigurer.XXssConfig::disable)
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .httpStrictTransportSecurity(
                        hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .referrerPolicy(
                        referrer -> referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserRepository userRepository,
                                                       PasswordEncoder passwordEncoder) {
        ApplicationUsernamePwdAuthenticationProvider authenticationProvider =
                new ApplicationUsernamePwdAuthenticationProvider(userRepository, passwordEncoder);
        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);
        return providerManager;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public AuthenticationEventPublisher authenticationEventPublisher(
            ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
    }
}
