package com.gatepass.controller;

import com.gatepass.dto.AuthDTO.RegisterRequest;
import com.gatepass.dto.GatepassDTO.GatepassResponse;
import com.gatepass.dto.GatepassDTO.StatsResponse;
import com.gatepass.entity.User;
import com.gatepass.service.GatepassService;
import com.gatepass.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final GatepassService gatepassService;
    private final UserService userService;

    // --- User Management ---

    /** Maps a User entity to a safe map without the password field. */
    private Map<String, Object> safeUser(User u) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("name", u.getName());
        m.put("email", u.getEmail());
        m.put("role", u.getRole());
        m.put("active", u.isActive());
        m.put("rollNumber", u.getRollNumber());
        m.put("roomNumber", u.getRoomNumber());
        m.put("phoneNumber", u.getPhoneNumber());
        m.put("department", u.getDepartment());
        m.put("year", u.getYear());
        m.put("employeeId", u.getEmployeeId());
        m.put("designation", u.getDesignation());
        m.put("createdAt", u.getCreatedAt());
        return m;
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(
            userService.getAllUsers().stream().map(this::safeUser).collect(Collectors.toList())
        );
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(safeUser(userService.createUser(request)));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(safeUser(userService.updateUser(id, request)));
    }

    @PatchMapping("/users/{id}/toggle-status")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // --- Gatepass Management ---
    
    @GetMapping("/gatepasses")
    public ResponseEntity<List<GatepassResponse>> getAllGatepasses() {
        return ResponseEntity.ok(gatepassService.getAllGatepasses());
    }

    @GetMapping("/gatepasses/search")
    public ResponseEntity<List<GatepassResponse>> searchGatepasses(@RequestParam String query) {
        return ResponseEntity.ok(gatepassService.searchGatepasses(query));
    }

    // --- Analytics ---
    
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(gatepassService.getStats());
    }
}
