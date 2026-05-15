package com.gatepass.controller;

import com.gatepass.dto.AuthDTO.*;
import com.gatepass.entity.User;
import com.gatepass.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Returns a safe user profile — never exposes the password hash.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole());
        profile.put("active", user.isActive());
        profile.put("rollNumber", user.getRollNumber());
        profile.put("roomNumber", user.getRoomNumber());
        profile.put("phoneNumber", user.getPhoneNumber());
        profile.put("department", user.getDepartment());
        profile.put("year", user.getYear());
        profile.put("employeeId", user.getEmployeeId());
        profile.put("designation", user.getDesignation());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("GatePass API is running ✅");
    }
}