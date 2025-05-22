package com.mlspamdetection.webapp_backend.security;

import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.service.UserService;
import com.mlspamdetection.webapp_backend.util.GoogleUserData;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Value("${app.oauth2.redirect-frontend-url}")
    private String redirectUri;

    public OAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            System.out.println("OAuth2 success handler triggered");

            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();

            // Get user details
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String googleId = oauth2User.getAttribute("sub");

            System.out.println("Google user: " + email);

            // Find or create user in your system
            GoogleUserData userData = new GoogleUserData();
            userData.setGoogleId(googleId);
            userData.setEmail(email);
            userData.setName(name);
            userData.setPictureUrl(oauth2User.getAttribute("picture"));

            User user = userService.findOrCreateGoogleUser(userData);

            // Generate JWT with roles
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();
            var roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(auth -> auth.replaceFirst("^ROLE_", ""))
                    .collect(Collectors.toList());
            String jwt = jwtUtil.generateToken(userDetails, roles);

            // Redirect to frontend with token
            String redirectUrl = redirectUri + "?token=" + jwt;
            System.out.println("Redirecting to: " + redirectUrl);

            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            System.err.println("Error in OAuth2 success handler: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
