package com.smartride.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * ============================================================
 * JwtService - Creates and Validates JWT Tokens
 * ============================================================
 *
 * 📌 What is a JWT Token?
 *    JWT = JSON Web Token
 *    It's like a digital "ID card" for logged-in users.
 *
 *    Structure: header.payload.signature
 *    Example:
 *      eyJhbGc....(header) . eyJ1c2Vy....(payload) . abc123...(signature)
 *
 *    The PAYLOAD contains: email (subject), issued time, expiry time.
 *    The SIGNATURE verifies the token was not tampered with.
 *
 * 📌 How JWT Auth Works in SmartRide:
 *    1. User logs in with email + password
 *    2. Server verifies credentials → creates JWT token
 *    3. Server sends token to frontend (React stores it in localStorage)
 *    4. For every future request, React sends token in header:
 *       Authorization: Bearer eyJhbGc...
 *    5. JwtAuthenticationFilter validates token → allows or denies request
 *
 * 📌 Benefit: Server doesn't store sessions!
 *    The token itself contains all needed info (stateless).
 * ============================================================
 */
@Service
public class JwtService {

    // Read secret key from application.properties (jwt.secret)
    @Value("${jwt.secret}")
    private String secretKey;

    // Read expiry time from application.properties (jwt.expiration)
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generate a JWT token for a user
     *
     * @param userDetails = the user (our User entity which implements UserDetails)
     * @return JWT token as string
     *
     * 📌 This now works because User implements UserDetails.
     *    userDetails.getUsername() returns the user's email.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Generate token with extra claims (additional info inside token payload)
     * Example: add role, userId etc. to the token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Builds the actual JWT token string
     * Using JJWT 0.12.x API (matches your pom.xml version)
     */
    private String buildToken(Map<String, Object> extraClaims,
                              UserDetails userDetails,
                              long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())                           // email goes here
                .issuedAt(new Date(System.currentTimeMillis()))               // current time
                .expiration(new Date(System.currentTimeMillis() + expiration)) // expiry time
                .signWith(getSigningKey())                                     // sign with secret
                .compact();                                                    // build the string
    }

    /**
     * Extract the email (username/subject) from a token
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validate a token against a user
     * Checks:
     *  1. Token's email matches the user's email
     *  2. Token is not expired
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Check if a token has passed its expiry time
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract any specific piece of info from a token
     * Uses a function to tell it WHICH claim to extract
     *
     * Example: extractClaim(token, Claims::getSubject) → gets the email
     * Example: extractClaim(token, Claims::getExpiration) → gets expiry date
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse the token and extract ALL claims from the payload
     * Also verifies the signature here
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // Verify the signature is correct
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Convert the secret key string (from application.properties)
     * into a cryptographic key object that JJWT can use
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}