package com.gatepass.repository;

import com.gatepass.entity.User;
import com.gatepass.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByRoleAndActive(Role role, boolean active);

    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
           "(LOWER(u.name) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(u.rollNumber) LIKE LOWER(CONCAT('%',:search,'%')))")
    List<User> searchByRoleAndKeyword(Role role, String search);

    long countByRole(Role role);
    long countByRoleAndActive(Role role, boolean active);
}