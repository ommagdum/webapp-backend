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

/**
 * Configuration class for application security.
 * 
 * <p>This class is the central configuration point for Spring Security in the application.
 * It defines the security rules, authentication mechanisms, and authorization requirements
 * for different API endpoints. The security implementation uses a combination of JWT-based
 * authentication and OAuth2 for social login capabilities.</p>
 * 
 * <p>Key security features implemented in this configuration:</p>
 * <ul>
 *   <li>JWT-based stateless authentication</li>
 *   <li>Role-based access control for API endpoints</li>
 *   <li>OAuth2 integration for social login</li>
 *   <li>Custom authentication success/failure handling</li>
 *   <li>CORS configuration integration</li>
 * </ul>
 * 
 * <p>The security rules are designed to protect sensitive endpoints while allowing public
 * access to authentication-related endpoints and frontend resources.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * User details service for loading user-specific data during authentication.
     */
    private final UserDetailsService userDetailsService;
    
    /**
     * Service for user-related operations, particularly for OAuth2 user creation and updates.
     */
    private final UserService userService;
    
    /**
     * Utility for JWT token generation, validation, and parsing.
     */
    private final JwtUtil jwtUtil;
    
    /**
     * Repository for accessing user data, used for token validation and user lookups.
     */
    private final UserRepository userRepository;
    
    /**
     * CORS filter for handling Cross-Origin Resource Sharing, applied before authentication.
     */
    private final CorsFilter corsFilter;

    /**
     * Constructs a new SecurityConfig with the required dependencies.
     *
     * @param userDetailsService service for loading user-specific data
     * @param userService service for user-related operations
     * @param jwtUtil utility for JWT operations
     * @param userRepository repository for user data access
     * @param corsFilter filter for handling Cross-Origin Resource Sharing
     */
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

    /**
     * Configures the security filter chain for the application.
     * 
     * <p>This method defines the core security rules and filters for the application, including:</p>
     * <ul>
     *   <li>CORS and CSRF configuration</li>
     *   <li>URL-based authorization rules</li>
     *   <li>Session management (stateless for JWT)</li>
     *   <li>OAuth2 login configuration</li>
     *   <li>Exception handling for unauthorized access</li>
     *   <li>Custom filter configuration</li>
     * </ul>
     * 
     * <p>The security rules are designed to protect API endpoints while allowing public
     * access to authentication endpoints and static resources. Admin-specific endpoints
     * are protected with role-based access control.</p>
     *
     * @param http the {@link HttpSecurity} to modify
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService, userRepository);

        http
                .cors(cors -> cors.disable()) // Disable Spring Security's CORS handling
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/ping").permitAll()
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

    /**
     * Creates and configures the OAuth2 authentication success handler.
     * 
     * <p>This handler is invoked when a user successfully authenticates via an OAuth2 provider
     * (such as Google, Facebook, etc.). It generates a JWT token for the authenticated user
     * and redirects them to the appropriate page in the frontend application.</p>
     *
     * @return the configured {@link OAuth2AuthenticationSuccessHandler}
     */
    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler(jwtUtil, userService);
    }

    /**
     * Configures the authentication provider for username/password authentication.
     * 
     * <p>This authentication provider uses the application's {@link UserDetailsService} to load
     * user details and the configured {@link PasswordEncoder} to validate passwords. It is used
     * for traditional username/password authentication flows.</p>
     *
     * @param passwordEncoder the password encoder to use for password validation
     * @return the configured {@link DaoAuthenticationProvider}
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Creates the authentication manager for the application.
     * 
     * <p>The authentication manager is responsible for processing authentication requests.
     * It delegates to the appropriate authentication providers based on the authentication type.</p>
     *
     * @param config the authentication configuration
     * @return the {@link AuthenticationManager}
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures the default prefix for granted authorities (roles).
     * 
     * <p>By default, Spring Security prepends the 'ROLE_' prefix to all role names.
     * This configuration removes that prefix requirement, allowing roles to be used
     * without the prefix in authorization expressions.</p>
     *
     * @return the {@link GrantedAuthorityDefaults} with an empty prefix
     */
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(""); // Removes ROLE_ prefix requirement
    }
}