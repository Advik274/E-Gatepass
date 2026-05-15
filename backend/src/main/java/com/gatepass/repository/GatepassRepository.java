package com.gatepass.repository;

import com.gatepass.entity.Gatepass;
import com.gatepass.enums.GatepassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GatepassRepository extends JpaRepository<Gatepass, Long> {

    Optional<Gatepass> findByGatepassNumber(String gatepassNumber);
    Optional<Gatepass> findByQrCode(String qrCode);

    List<Gatepass> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<Gatepass> findByStatusOrderByCreatedAtDesc(GatepassStatus status);
    List<Gatepass> findByStatusInOrderByCreatedAtDesc(List<GatepassStatus> statuses);

    List<Gatepass> findByStatusAndUrgentTrueOrderByCreatedAtAsc(GatepassStatus status);

    @Query("SELECT g FROM Gatepass g WHERE g.status = 'ACTIVE' AND g.expectedReturnDateTime < :now")
    List<Gatepass> findOverdueGatepasses(@Param("now") LocalDateTime now);

    @Query("SELECT g FROM Gatepass g WHERE g.status = 'ACTIVE'")
    List<Gatepass> findCurrentlyOutside();

    @Query("SELECT COUNT(g) FROM Gatepass g WHERE g.status = :status")
    long countByStatus(@Param("status") GatepassStatus status);

    @Query("SELECT COUNT(g) FROM Gatepass g WHERE g.createdAt >= :start AND g.createdAt <= :end")
    long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT g FROM Gatepass g WHERE g.student.id = :studentId AND g.status NOT IN ('PENDING', 'COORDINATOR_REJECTED', 'WARDEN_REJECTED')")
    List<Gatepass> findApprovedByStudent(@Param("studentId") Long studentId);

    @Query("SELECT g FROM Gatepass g WHERE " +
           "(LOWER(g.student.name) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(g.student.rollNumber) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(g.gatepassNumber) LIKE LOWER(CONCAT('%',:search,'%')))")
    List<Gatepass> searchGatepasses(@Param("search") String search);

    @Query("SELECT g FROM Gatepass g WHERE g.createdAt >= :start ORDER BY g.createdAt DESC")
    List<Gatepass> findRecentGatepasses(@Param("start") LocalDateTime start);

    long countByStudentId(Long studentId);
}