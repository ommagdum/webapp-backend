package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsible for JWT (JSON Web Token) operations.
 * 
 * <p>This service provides functionality for generating, validating, and parsing JWT tokens
 * used for authentication and authorization in the application. It handles both access tokens
 * (short-lived) and refresh tokens (long-lived).</p>
 * 
 * <p>The service uses HMAC-SHA for signing tokens and includes user-specific claims such as
 * email (as subject) and role. Token expiration times are configurable via application properties.</p>
 */
@Service
public class JwtService {

    /**
     * Secret key used for signing JWT tokens.
     * Injected from application properties.
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Expiration time for access tokens in milliseconds.
     * Typically set to 15 minutes.
     */
    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration; 

    /**
     * Expiration time for refresh tokens in milliseconds.
     * Typically set to 7 days.
     */
    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    /**
     * Creates a signing key from the secret key.
     * 
     * <p>This method converts the string secret key into a cryptographic key
     * suitable for HMAC-SHA signing of JWT tokens.</p>
     *
     * @return the signing key used for JWT token operations
     */
    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts the username (email) from a JWT token.
     * 
     * <p>The username is stored as the subject claim in the token.</p>
     *
     * @param token the JWT token to extract the username from
     * @return the username (email) extracted from the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token to extract the expiration date from
     * @return the expiration date of the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a JWT token using a claims resolver function.
     * 
     * <p>This generic method allows for extracting any claim from the token by
     * providing a function that specifies which claim to extract.</p>
     *
     * @param <T> the type of the claim value to extract
     * @param token the JWT token to extract the claim from
     * @param claimsResolver a function that extracts the desired claim from the claims object
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a JWT token.
     * 
     * <p>This method parses the token, verifies its signature using the signing key,
     * and returns all claims contained in the token body.</p>
     *
     * @param token the JWT token to extract claims from
     * @return all claims contained in the token
     * @throws io.jsonwebtoken.JwtException if the token is invalid or has an invalid signature
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if a JWT token has expired.
     * 
     * <p>A token is considered expired if its expiration date is before the current date.</p>
     *
     * @param token the JWT token to check
     * @return true if the token has expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generates a new access token for a user.
     * 
     * <p>Access tokens are short-lived tokens used for authenticating API requests.
     * They contain the user's email as the subject and their role as a custom claim.</p>
     * 
     * <p>The token is signed with the application's signing key and includes:</p>
     * <ul>
     *   <li>User's email as the subject</li>
     *   <li>User's role as a custom claim</li>
     *   <li>Issued at timestamp</li>
     *   <li>Expiration timestamp (typically 15 minutes from issuance)</li>
     * </ul>
     *
     * @param user the user to generate the token for
     * @return a signed JWT access token
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generates a new refresh token for a user.
     * 
     * <p>Refresh tokens are long-lived tokens used for obtaining new access tokens
     * without requiring the user to log in again. They contain only the user's email
     * as the subject and no additional claims.</p>
     * 
     * <p>The token is signed with the application's signing key and includes:</p>
     * <ul>
     *   <li>User's email as the subject</li>
     *   <li>Issued at timestamp</li>
     *   <li>Expiration timestamp (typically 7 days from issuance)</li>
     * </ul>
     *
     * @param user the user to generate the token for
     * @return a signed JWT refresh token
     */
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates a JWT token.
     * 
     * <p>This method checks if the token:</p>
     * <ul>
     *   <li>Has a valid signature (using the application's signing key)</li>
     *   <li>Has not expired</li>
     * </ul>
     * 
     * <p>If any validation fails, the method returns false.</p>
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the user role from a JWT token.
     * 
     * <p>The role is stored as a custom claim named "role" in the token.</p>
     *
     * @param token the JWT token to extract the role from
     * @return the user role extracted from the token, or null if not present
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
}
