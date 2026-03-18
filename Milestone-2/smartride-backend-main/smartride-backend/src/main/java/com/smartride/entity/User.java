package com.smartride.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

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
    @Column(nullable = false)
    private Boolean verified = false;

    // Account active flag
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
    private Double rating = 0.0;

    private Integer totalRatings = 0;

    /**
     * Driver wallet balance (in local currency, e.g., INR)
     * Used to track earnings and payouts for drivers.
     */
    @Column(name = "driver_wallet_balance", nullable = false)
    private Double driverWalletBalance = 0.0;

    /**
     * 📌 @PrePersist - Runs AUTOMATICALLY before saving to DB
     *    Sets the createdAt and updatedAt timestamps.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Ensure wallet balance is never null in DB
        if (driverWalletBalance == null) {
            driverWalletBalance = 0.0;
        }

        // Ensure verified flag is always set (avoids missing-column insert failures)
        if (verified == null) {
            verified = false;
        }
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
        return verified != null && verified;
    }

    /**
     * The Role enum - defines the two types of users in SmartRide
     */
    public enum Role {
        DRIVER,
        PASSENGER
    }

    // ---------- Generated Getters / Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public Integer getVehicleCapacity() { return vehicleCapacity; }
    public void setVehicleCapacity(Integer vehicleCapacity) { this.vehicleCapacity = vehicleCapacity; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }

    public Double getDriverWalletBalance() { return driverWalletBalance; }
    public void setDriverWalletBalance(Double driverWalletBalance) { this.driverWalletBalance = driverWalletBalance; }
}
