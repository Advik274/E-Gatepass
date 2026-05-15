package com.gatepass.service;

import com.gatepass.dto.AuthDTO.RegisterRequest;
import com.gatepass.entity.User;
import com.gatepass.enums.Role;
import com.gatepass.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    @Transactional
    public User createUser(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered: " + req.getEmail());
        }
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .rollNumber(req.getRollNumber())
                .roomNumber(req.getRoomNumber())
                .phoneNumber(req.getPhoneNumber())
                .parentPhone(req.getParentPhone())
                .department(req.getDepartment())
                .year(req.getYear())
                .employeeId(req.getEmployeeId())
                .designation(req.getDesignation())
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, RegisterRequest req) {
        User user = getUserById(id);
        if (req.getName() != null) user.setName(req.getName());
        if (req.getPhoneNumber() != null) user.setPhoneNumber(req.getPhoneNumber());
        if (req.getParentPhone() != null) user.setParentPhone(req.getParentPhone());
        if (req.getDepartment() != null) user.setDepartment(req.getDepartment());
        if (req.getYear() != null) user.setYear(req.getYear());
        if (req.getRoomNumber() != null) user.setRoomNumber(req.getRoomNumber());
        if (req.getDesignation() != null) user.setDesignation(req.getDesignation());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        return userRepository.save(user);
    }

    @Transactional
    public void toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<User> searchStudents(String keyword) {
        return userRepository.searchByRoleAndKeyword(Role.STUDENT, keyword);
    }

    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }
}