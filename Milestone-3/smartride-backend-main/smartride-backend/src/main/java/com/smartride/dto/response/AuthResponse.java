package com.smartride.dto.response;

import com.smartride.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType;
    private UserResponse user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String fullName;
        private String email;
        private String phone;
        private User.Role role;
        private Boolean emailVerified;

        // Driver-specific fields
        private String carModel;
        private String licensePlate;
        private Integer vehicleCapacity;
        private String vehicleType;
    }
}