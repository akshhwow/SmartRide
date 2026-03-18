package com.smartride.config;

import com.smartride.security.JwtAuthenticationFilter;
import com.smartride.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * ============================================================
 * SecurityConfig - The Security "Rulebook" for Your App
 * ============================================================
 *
 * 📌 WHY THE CIRCULAR DEPENDENCY HAPPENS:
 *
 *    JwtAuthenticationFilter needs → UserDetailsService (from SecurityConfig)
 *    SecurityConfig needs          → JwtAuthenticationFilter
 *
 *    So Spring gets confused:
 *    "To create JwtAuthenticationFilter, I need SecurityConfig.
 *     To create SecurityConfig, I need JwtAuthenticationFilter.
 *     I can't create either one first!" → CRASH
 *
 * 📌 HOW WE FIX IT — @Lazy annotation:
 *
 *    We tell Spring: "Don't inject JwtAuthenticationFilter immediately
 *    when creating SecurityConfig. Wait until it's actually NEEDED
 *    (i.e., when securityFilterChain() method runs)."
 *
 *    By that time, all other beans are already created, so the cycle
 *    is broken. Spring can now create everything in the right order.
 *
 *    This is the cleanest, most standard solution — no ApplicationContext
 *    hacks, no allow-circular-references=true needed.
 * ============================================================
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    // ✅ THE FIX: @Lazy tells Spring to inject this bean ONLY when it's first used,
    //    not at startup. This breaks the circular dependency cycle.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    // We use @Autowired constructor manually (instead of @RequiredArgsConstructor from Lombok)
    // so we can apply @Lazy to ONLY the JwtAuthenticationFilter parameter.
    // @Lazy on a single parameter = "inject this one lazily, inject others normally"
    @Autowired
    public SecurityConfig(UserRepository userRepository,
                          @Lazy JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userRepository = userRepository;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 📌 UserDetailsService - Tells Spring Security HOW to load a user
     *
     *    When Spring Security needs to find a user (during JWT validation),
     *    it calls this method with the email address.
     *    We look up the user in the database and return the User entity.
     *
     *    ✅ We return the User entity directly because it now implements
     *    UserDetails — no need to wrap it in a generic Spring User object.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + username));
    }

    /**
     * 📌 PasswordEncoder - BCrypt password hashing
     *
     *    Passwords are NEVER stored as plain text.
     *    BCrypt converts "mypassword" → "$2a$10$randomhash..."
     *    You can verify a password against the hash but can NEVER reverse it.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 📌 AuthenticationProvider - Wires together UserDetailsService + PasswordEncoder
     *
     *    When a user tries to log in:
     *    1. Loads user via userDetailsService()
     *    2. Compares entered password against stored BCrypt hash
     *    3. Match → authentication succeeds
     *    4. No match → authentication fails → login error returned
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 📌 AuthenticationManager - The main authentication coordinator
     *
     *    AuthService uses this during login:
     *    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(...))
     *    It delegates to our AuthenticationProvider above.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 📌 SecurityFilterChain - The complete security rules for your application
     *
     *    This defines:
     *    - CSRF: Disabled (not needed with JWT)
     *    - CORS: Enabled (allows React on port 3000 to call this backend)
     *    - Public endpoints: /api/auth/** (register, login, verify-otp, resend-otp)
     *    - Protected endpoints: Everything else requires a valid JWT token
     *    - Sessions: STATELESS (no server-side sessions, JWT handles everything)
     *    - Filter: JwtAuthenticationFilter runs before every protected request
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed because we use JWT, not browser cookies
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS using our corsConfigurationSource() bean
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Define which endpoints are public and which need authentication
                .authorizeHttpRequests(auth -> auth

                        // ✅ PUBLIC - No JWT token needed for these endpoints:
                        .requestMatchers(
                                "/api/auth/**",                  // register, login, verify-otp, resend-otp
                                "/api/rides/search",             // anyone can browse available rides
                                "/api/ratings/driver/*/average", // anyone can see driver ratings
                                "/api/ratings/driver/*/reviews"  // anyone can see driver reviews
                        ).permitAll()

                        // 🔒 PROTECTED - All other endpoints require a valid JWT token
                        .anyRequest().authenticated()
                )

                // STATELESS: Spring will never create an HttpSession for authentication.
                // Every single request must carry a valid JWT token.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Register our custom AuthenticationProvider
                .authenticationProvider(authenticationProvider())

                // Add JWT filter to run BEFORE Spring's built-in UsernamePasswordAuthenticationFilter.
                // Our filter validates the JWT and sets the authenticated user in SecurityContext
                // BEFORE Spring Security checks if the user is authenticated.
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 📌 CORS Configuration - Allow React frontend to talk to this backend
     *
     *    CORS = Cross-Origin Resource Sharing
     *
     *    Problem: Browser BLOCKS requests from http://localhost:3000 (React)
     *             to http://localhost:8080 (Spring Boot).
     *             Different ports = different "origins" = browser blocks it!
     *
     *    Solution: This config tells the browser:
     *             "It's okay! The backend allows requests from localhost:3000"
     *
     *    The allowed origin is read from application.properties:
     *    app.cors.allowed-origins=http://localhost:3000
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from React frontend (value from application.properties)
        configuration.setAllowedOrigins(List.of(allowedOrigins));

        // Allow these HTTP methods
        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Allow these request headers
        // Authorization = where the JWT token is sent
        // Content-Type  = tells the server the request body is JSON
        configuration.setAllowedHeaders(
                Arrays.asList("Authorization", "Content-Type", "Accept"));

        // Allow cookies/credentials in cross-origin requests
        configuration.setAllowCredentials(true);

        // Cache CORS preflight response for 1 hour to reduce OPTIONS requests
        configuration.setMaxAge(3600L);

        // Apply this CORS config to ALL endpoints in the application
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}