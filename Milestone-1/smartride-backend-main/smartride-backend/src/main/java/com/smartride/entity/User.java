package com.smartride.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * ============================================================
 * USER ENTITY - Represents the 'users' table in your database
 * ============================================================
 *
 * 📌 What is an Entity?
 *    An Entity is a Java class that maps to a database TABLE.
 *    Each field in this class = a COLUMN in the database table.
 *    Each object of this class = a ROW in the database table.
 *
 * 📌 WHY implements UserDetails?
 *    Spring Security needs every user object to implement UserDetails.
 *    This tells Spring Security HOW to:
 *      - Get the username (we use email)
 *      - Get the password (for verification)
 *      - Get the roles/authorities (DRIVER or PASSENGER)
 *      - Check if account is active/locked/expired
 *
 *    Without this, you get:
 *    "User cannot be converted to UserDetails" ← YOUR EXACT ERROR
 *
 * 📌 Lombok Annotations (saves you from writing repetitive code):
 *    @Data        = Auto-generates getters, setters, toString, equals
 *    @Builder     = Allows: User.builder().name("John").build()
 *    @NoArgsConstructor  = Auto-generates empty constructor
 *    @AllArgsConstructor = Auto-generates constructor with all fields
 *
 * 📌 JPA Annotations:
 *    @Entity      = Tells Spring this class is a database table
 *    @Table       = Specifies the actual table name in MySQL
 *    @Id          = This field is the Primary Key
 *    @GeneratedValue = Auto-increment the ID (1, 2, 3, ...)
 *    @Column      = Maps field to a specific column with constraints
 *    @Enumerated  = Stores enum as String in database
 * ============================================================
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Full name of the user
    @Column(nullable = false, length = 100)
    private String fullName;

    // Email - must be unique (no two users with same email)
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Phone number - must be unique
    @Column(unique = true, length = 15)
    private String phone;

    // Password stored as encrypted hash (never store plain text!)
    @Column(nullable = false)
    private String password;

    // Role: DRIVER or PASSENGER
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Email verified flag - user can't login until email is verified
    @Builder.Default
    @Column(nullable = false)
    private Boolean emailVerified = false;

    // Account active flag
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    // When the account was created (auto-set)
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Last update time
    private LocalDateTime updatedAt;

    // === DRIVER-SPECIFIC FIELDS ===
    // These fields are only used when role = DRIVER

    private String carModel;         // e.g., "Honda Civic"
    private String licensePlate;     // e.g., "MH-12-AB-1234"
    private Integer vehicleCapacity; // Total seats in car, e.g., 4
    private String vehicleType;      // e.g., "Sedan", "SUV", "Hatchback"

    // Driver rating (average of all reviews)
    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private Integer totalRatings = 0;

    /**
     * 📌 @PrePersist - Runs AUTOMATICALLY before saving to DB
     *    Sets the createdAt and updatedAt timestamps.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 📌 @PreUpdate - Runs AUTOMATICALLY before updating a record in DB
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // =========================================================
    // ✅ UserDetails Methods - Required by Spring Security
    // =========================================================

    /**
     * Returns the roles/permissions of this user.
     * Spring Security uses this to check access (e.g., ROLE_DRIVER, ROLE_PASSENGER)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Returns the "username" used for authentication.
     * In our app, we use EMAIL as the username.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Returns the encrypted password.
     * Lombok @Data generates getPassword() but we explicitly override
     * to make it clear Spring Security uses this.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Is the account NOT expired?
     * Return true = account is valid (not expired).
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Is the account NOT locked?
     * We use isActive field - if active=true, account is not locked.
     */
    @Override
    public boolean isAccountNonLocked() {
        return isActive != null && isActive;
    }

    /**
     * Are the credentials (password) NOT expired?
     * Return true = password is still valid.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Is the account enabled?
     * We check emailVerified - user must verify email to be enabled.
     */
    @Override
    public boolean isEnabled() {
        return emailVerified != null && emailVerified;
    }

    /**
     * The Role enum - defines the two types of users in SmartRide
     */
    public enum Role {
        DRIVER,
        PASSENGER
    }
}