package com.smartride.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ============================================================
 * JwtAuthenticationFilter - Intercepts Every Request
 * ============================================================
 *
 * 📌 What does this filter do?
 *    Every time a request comes to your server, this filter runs FIRST.
 *    It checks if the request has a valid JWT token in the header.
 *    If YES → marks the user as "authenticated" in Spring Security.
 *    If NO  → lets request pass (SecurityConfig will block protected routes).
 *
 * 📌 Flow:
 *    Request arrives
 *      → Filter reads Authorization header
 *      → Extracts JWT token (removes "Bearer " prefix)
 *      → Extracts email from token
 *      → Loads user from DB
 *      → Validates token (correct signature? not expired? matches user?)
 *      → Sets user as authenticated in SecurityContext
 *      → Request continues to Controller
 *
 * 📌 OncePerRequestFilter:
 *    Ensures this filter runs exactly ONCE per request (not multiple times).
 *
 * 📌 @Component:
 *    Registers this as a Spring Bean so it can be injected in SecurityConfig.
 * ============================================================
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1: Get the Authorization header
        // Expected format: "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIi..."
        final String authHeader = request.getHeader("Authorization");

        // Step 2: If no Authorization header or doesn't start with "Bearer ", skip
        // This happens for public endpoints like /api/auth/register and /api/auth/login
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract just the token (skip the first 7 characters = "Bearer ")
        final String jwt = authHeader.substring(7);

        // Step 4: Extract the email address from the token's payload
        final String userEmail = jwtService.extractEmail(jwt);

        // Step 5: If we found an email AND the user is not already authenticated
        //         (prevents re-authenticating on every filter in the chain)
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Step 6: Load the full user object from database
            // This calls UserDetailsService → finds User by email → returns User entity
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Step 7: Validate the token
            // Checks: correct signature? not expired? email matches?
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Step 8: Create a Spring Security authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // credentials not needed after auth
                                userDetails.getAuthorities()   // roles: ROLE_DRIVER / ROLE_PASSENGER
                        );

                // Attach request details (IP address, session info) to the auth token
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 9: Mark the user as authenticated in Spring Security
                // After this line, Spring Security knows who made this request.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 10: Continue processing the request
        filterChain.doFilter(request, response);
    }
}