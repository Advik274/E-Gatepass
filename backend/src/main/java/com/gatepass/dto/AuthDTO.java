package com.gatepass.dto;

import com.gatepass.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AuthDTO {

    @Data
    public static class LoginRequest {
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String name;
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String password;
        private Role role;
        private String rollNumber;
        private String roomNumber;
        private String phoneNumber;
        private String parentPhone;
        private String department;
        private Integer year;
        private String employeeId;
        private String designation;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String email;
        private String name;
        private Role role;
        private Long userId;

        public AuthResponse(String token, String email, String name, Role role, Long userId) {
            this.token = token;
            this.email = email;
            this.name = name;
            this.role = role;
            this.userId = userId;
        }
    }
}