package com.mlspamdetection.webapp_backend.config;

import com.mlspamdetection.webapp_backend.repo.UserRepository;
import com.mlspamdetection.webapp_backend.security.JwtAuthenticationFilter;
import com.mlspamdetection.webapp_backend.security.JwtUtil;
import com.mlspamdetection.webapp_backend.security.OAuth2AuthenticationSuccessHandler;
import com.mlspamdetection.webapp_backend.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final CorsFilter corsFilter;

    public SecurityConfig(UserDetailsService userDetailsService,
                          UserService userService,
                          JwtUtil jwtUtil,
                          UserRepository userRepository,
                          CorsFilter corsFilter) {
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.corsFilter = corsFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService, userRepository);

        http
                .cors(cors -> cors.disable()) // Disable Spring Security's CORS handling
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/register-admin").permitAll()
                        .requestMatchers("/api/auth/**", "/login/oauth2/**", "/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/feedback").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/admin/**").hasAuthority("ROLE_ADMIN") // Admin-only endpoints
                        .requestMatchers("/api/**").authenticated() // API endpoints
                        .anyRequest().permitAll() // Frontend routes
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2AuthenticationSuccessHandler())
                        .failureHandler((request, response, exception) -> {
                            response.sendRedirect("/login?error");
                        })
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api")) {
                                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                )
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class) // Add custom CORS filter before authentication filter
                .addFilterAt(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler(jwtUtil, userService);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(""); // Removes ROLE_ prefix requirement
    }
}