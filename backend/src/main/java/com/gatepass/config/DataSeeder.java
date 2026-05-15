package com.gatepass.config;

import com.gatepass.entity.User;
import com.gatepass.enums.Role;
import com.gatepass.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        log.info("Seeding demo data...");

        // Admin
        userRepository.save(User.builder()
                .name("System Admin")
                .email("admin@college.edu")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .employeeId("ADMIN001")
                .designation("System Administrator")
                .build());

        // Warden
        userRepository.save(User.builder()
                .name("Dr. Rajesh Kumar")
                .email("warden@college.edu")
                .password(passwordEncoder.encode("Warden@123"))
                .role(Role.WARDEN)
                .employeeId("WRD001")
                .designation("Chief Warden")
                .phoneNumber("9876543210")
                .build());

        // Coordinator
        userRepository.save(User.builder()
                .name("Prof. Priya Sharma")
                .email("coordinator@college.edu")
                .password(passwordEncoder.encode("Coord@123"))
                .role(Role.COORDINATOR)
                .employeeId("CRD001")
                .designation("Hostel Coordinator")
                .phoneNumber("9876543211")
                .build());

        // Security Guard
        userRepository.save(User.builder()
                .name("Ramesh Guard")
                .email("security@college.edu")
                .password(passwordEncoder.encode("Security@123"))
                .role(Role.SECURITY)
                .employeeId("SEC001")
                .designation("Security Guard")
                .phoneNumber("9876543212")
                .build());

        // Students
        userRepository.save(User.builder()
                .name("Arnav Sharma")
                .email("student@college.edu")
                .password(passwordEncoder.encode("Student@123"))
                .role(Role.STUDENT)
                .rollNumber("CS2021001")
                .roomNumber("A-101")
                .phoneNumber("9876543213")
                .parentPhone("9876543214")
                .department("Computer Science")
                .year(3)
                .build());

        userRepository.save(User.builder()
                .name("Priya Patel")
                .email("priya@college.edu")
                .password(passwordEncoder.encode("Student@123"))
                .role(Role.STUDENT)
                .rollNumber("CS2021002")
                .roomNumber("B-205")
                .phoneNumber("9876543215")
                .parentPhone("9876543216")
                .department("Computer Science")
                .year(3)
                .build());

        userRepository.save(User.builder()
                .name("Rohit Singh")
                .email("rohit@college.edu")
                .password(passwordEncoder.encode("Student@123"))
                .role(Role.STUDENT)
                .rollNumber("EC2021003")
                .roomNumber("C-310")
                .phoneNumber("9876543217")
                .parentPhone("9876543218")
                .department("Electronics")
                .year(2)
                .build());

        log.info("✅ Demo data seeded successfully!");
        log.info("📧 Login credentials:");
        log.info("  Admin      → admin@college.edu / Admin@123");
        log.info("  Warden     → warden@college.edu / Warden@123");
        log.info("  Coordinator → coordinator@college.edu / Coord@123");
        log.info("  Security   → security@college.edu / Security@123");
        log.info("  Student    → student@college.edu / Student@123");
    }
}