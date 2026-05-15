package com.gatepass.service;

import com.gatepass.dto.GatepassDTO.*;
import com.gatepass.entity.AuditLog;
import com.gatepass.entity.Gatepass;
import com.gatepass.entity.User;
import com.gatepass.enums.GatepassStatus;
import com.gatepass.repository.AuditLogRepository;
import com.gatepass.repository.GatepassRepository;
import com.gatepass.repository.UserRepository;
import com.gatepass.util.QRCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GatepassService {

    private final GatepassRepository gatepassRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final QRCodeUtil qrCodeUtil;

    // ─── Student Operations ────────────────────────────────────────────────────

    @Transactional
    public GatepassResponse createGatepass(CreateRequest req, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        String gatepassNumber = generateGatepassNumber();

        Gatepass gatepass = Gatepass.builder()
                .gatepassNumber(gatepassNumber)
                .student(student)
                .destination(req.getDestination())
                .reason(req.getReason())
                .outDateTime(req.getOutDateTime())
                .expectedReturnDateTime(req.getExpectedReturnDateTime())
                .parentName(req.getParentName())
                .parentPhone(req.getParentPhone())
                .parentRelation(req.getParentRelation())
                .urgent(req.isUrgent())
                .additionalNotes(req.getAdditionalNotes())
                .status(GatepassStatus.PENDING)
                .build();

        gatepass = gatepassRepository.save(gatepass);
        audit(student.getId(), student.getEmail(), "GATEPASS_CREATED", "Gatepass", gatepass.getId(),
                "Created gatepass " + gatepassNumber);

        return toResponse(gatepass);
    }

    public List<GatepassResponse> getStudentGatepasses(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return gatepassRepository.findByStudentIdOrderByCreatedAtDesc(student.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void cancelGatepass(Long id, String studentEmail) {
        Gatepass gatepass = getGatepassById(id);
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!gatepass.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Not authorized to cancel this gatepass");
        }
        if (gatepass.getStatus() != GatepassStatus.PENDING) {
            throw new RuntimeException("Only PENDING gatepasses can be cancelled");
        }
        gatepass.setStatus(GatepassStatus.CANCELLED);
        gatepassRepository.save(gatepass);
        audit(student.getId(), student.getEmail(), "GATEPASS_CANCELLED", "Gatepass", id, "Cancelled by student");
    }

    // ─── Coordinator Operations ────────────────────────────────────────────────

    public List<GatepassResponse> getPendingForCoordinator() {
        return gatepassRepository.findByStatusOrderByCreatedAtDesc(GatepassStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public GatepassResponse coordinatorReview(Long id, ReviewRequest req, String coordinatorEmail) {
        Gatepass gatepass = getGatepassById(id);
        User coordinator = userRepository.findByEmail(coordinatorEmail)
                .orElseThrow(() -> new RuntimeException("Coordinator not found"));

        if (gatepass.getStatus() != GatepassStatus.PENDING) {
            throw new RuntimeException("Gatepass is not in PENDING state");
        }

        gatepass.setCoordinator(coordinator);
        gatepass.setCoordinatorReviewedAt(LocalDateTime.now());
        gatepass.setCoordinatorRemarks(req.getRemarks());
        gatepass.setStatus(req.isApproved()
                ? GatepassStatus.COORDINATOR_APPROVED
                : GatepassStatus.COORDINATOR_REJECTED);

        gatepass = gatepassRepository.save(gatepass);
        audit(coordinator.getId(), coordinator.getEmail(),
                req.isApproved() ? "COORDINATOR_APPROVED" : "COORDINATOR_REJECTED",
                "Gatepass", id, req.getRemarks());

        return toResponse(gatepass);
    }

    // ─── Warden Operations ────────────────────────────────────────────────────

    public List<GatepassResponse> getPendingForWarden() {
        return gatepassRepository.findByStatusOrderByCreatedAtDesc(GatepassStatus.COORDINATOR_APPROVED)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<GatepassResponse> getAllForWarden() {
        return gatepassRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public GatepassResponse wardenReview(Long id, ReviewRequest req, String wardenEmail) {
        Gatepass gatepass = getGatepassById(id);
        User warden = userRepository.findByEmail(wardenEmail)
                .orElseThrow(() -> new RuntimeException("Warden not found"));

        if (gatepass.getStatus() != GatepassStatus.COORDINATOR_APPROVED) {
            throw new RuntimeException("Gatepass is not in COORDINATOR_APPROVED state");
        }

        gatepass.setWarden(warden);
        gatepass.setWardenReviewedAt(LocalDateTime.now());
        gatepass.setWardenRemarks(req.getRemarks());

        if (req.isApproved()) {
            gatepass.setStatus(GatepassStatus.WARDEN_APPROVED);
            // Generate QR code
            String timestamp = LocalDateTime.now().toString();
            String qrHash = qrCodeUtil.generateQRHash(
                    gatepass.getGatepassNumber(),
                    gatepass.getStudent().getId(),
                    timestamp);
            String qrImage = qrCodeUtil.generateQRCodeBase64(qrHash);
            gatepass.setQrCode(qrHash);
            gatepass.setQrCodeImage(qrImage);
        } else {
            gatepass.setStatus(GatepassStatus.WARDEN_REJECTED);
        }

        gatepass = gatepassRepository.save(gatepass);
        audit(warden.getId(), warden.getEmail(),
                req.isApproved() ? "WARDEN_APPROVED" : "WARDEN_REJECTED",
                "Gatepass", id, req.getRemarks());

        return toResponse(gatepass);
    }

    // ─── Security Operations ───────────────────────────────────────────────────

    public List<GatepassResponse> getCurrentlyOutside() {
        return gatepassRepository.findCurrentlyOutside()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<GatepassResponse> getOverdueGatepasses() {
        return gatepassRepository.findOverdueGatepasses(LocalDateTime.now())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ScanResponse scanGatepass(ScanRequest req, String guardEmail) {
        User guard = userRepository.findByEmail(guardEmail)
                .orElseThrow(() -> new RuntimeException("Guard not found"));

        Gatepass gatepass = null;

        if (req.getQrCode() != null && !req.getQrCode().isBlank()) {
            gatepass = gatepassRepository.findByQrCode(req.getQrCode()).orElse(null);
        }
        if (gatepass == null && req.getGatepassNumber() != null && !req.getGatepassNumber().isBlank()) {
            gatepass = gatepassRepository.findByGatepassNumber(req.getGatepassNumber()).orElse(null);
        }

        if (gatepass == null) {
            ScanResponse resp = new ScanResponse();
            resp.setValid(false);
            resp.setMessage("Gatepass not found — invalid QR or number");
            return resp;
        }

        ScanResponse resp = new ScanResponse();

        if (gatepass.getStatus() == GatepassStatus.WARDEN_APPROVED) {
            // EXIT scan
            gatepass.setStatus(GatepassStatus.ACTIVE);
            gatepass.setExitGuard(guard);
            gatepass.setExitScannedAt(LocalDateTime.now());
            gatepassRepository.save(gatepass);
            audit(guard.getId(), guard.getEmail(), "EXIT_SCANNED", "Gatepass", gatepass.getId(),
                    "Student exited: " + gatepass.getStudent().getName());
            resp.setValid(true);
            resp.setAction("EXIT");
            resp.setMessage("✅ Exit verified — " + gatepass.getStudent().getName() + " is now outside");

        } else if (gatepass.getStatus() == GatepassStatus.ACTIVE
                || gatepass.getStatus() == GatepassStatus.OVERDUE) {
            // RETURN scan
            LocalDateTime now = LocalDateTime.now();
            boolean overdue = now.isAfter(gatepass.getExpectedReturnDateTime());
            gatepass.setStatus(GatepassStatus.COMPLETED);
            gatepass.setActualReturnDateTime(now);
            gatepass.setReturnGuard(guard);
            gatepass.setReturnScannedAt(now);
            gatepassRepository.save(gatepass);
            audit(guard.getId(), guard.getEmail(), "RETURN_SCANNED", "Gatepass", gatepass.getId(),
                    "Student returned: " + gatepass.getStudent().getName() + (overdue ? " (OVERDUE)" : ""));
            resp.setValid(true);
            resp.setAction("RETURN");
            resp.setMessage(overdue
                    ? "⚠️ Return verified (OVERDUE) — " + gatepass.getStudent().getName()
                    : "✅ Return verified — " + gatepass.getStudent().getName() + " is back");

        } else {
            resp.setValid(false);
            resp.setMessage("❌ Gatepass is not valid for scanning (status: " + gatepass.getStatus() + ")");
        }

        resp.setGatepass(toResponse(gatepass));
        return resp;
    }

    // ─── Admin Operations ──────────────────────────────────────────────────────

    public List<GatepassResponse> getAllGatepasses() {
        return gatepassRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<GatepassResponse> searchGatepasses(String query) {
        return gatepassRepository.searchGatepasses(query)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public StatsResponse getStats() {
        StatsResponse stats = new StatsResponse();
        stats.setTotalGatepasses(gatepassRepository.count());
        stats.setPending(gatepassRepository.countByStatus(GatepassStatus.PENDING));
        stats.setCoordinatorApproved(gatepassRepository.countByStatus(GatepassStatus.COORDINATOR_APPROVED));
        stats.setWardenApproved(gatepassRepository.countByStatus(GatepassStatus.WARDEN_APPROVED));
        stats.setActive(gatepassRepository.countByStatus(GatepassStatus.ACTIVE));
        stats.setCompleted(gatepassRepository.countByStatus(GatepassStatus.COMPLETED));
        stats.setOverdue(gatepassRepository.countByStatus(GatepassStatus.OVERDUE));
        stats.setRejected(gatepassRepository.countByStatus(GatepassStatus.COORDINATOR_REJECTED)
                + gatepassRepository.countByStatus(GatepassStatus.WARDEN_REJECTED));
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        stats.setTodayTotal(gatepassRepository.countByDateRange(todayStart, LocalDateTime.now()));
        stats.setCurrentlyOutside(gatepassRepository.findCurrentlyOutside().size());
        return stats;
    }

    // ─── Bulk Operations ───────────────────────────────────────────────────────

    @Transactional
    public void bulkCoordinatorApprove(List<Long> ids, String coordinatorEmail) {
        ids.forEach(id -> {
            try {
                ReviewRequest req = new ReviewRequest();
                req.setApproved(true);
                req.setRemarks("Bulk approved");
                coordinatorReview(id, req, coordinatorEmail);
            } catch (Exception e) {
                log.warn("Could not approve gatepass {}: {}", id, e.getMessage());
            }
        });
    }

    @Transactional
    public void bulkWardenApprove(List<Long> ids, String wardenEmail) {
        ids.forEach(id -> {
            try {
                ReviewRequest req = new ReviewRequest();
                req.setApproved(true);
                req.setRemarks("Bulk approved");
                wardenReview(id, req, wardenEmail);
            } catch (Exception e) {
                log.warn("Could not approve gatepass {}: {}", id, e.getMessage());
            }
        });
    }

    // ─── Scheduled: mark overdue ───────────────────────────────────────────────

    @Scheduled(fixedRate = 60000) // runs every 60 seconds
    @Transactional
    public void markOverdueGatepasses() {
        List<Gatepass> overdue = gatepassRepository.findOverdueGatepasses(LocalDateTime.now());
        overdue.forEach(g -> {
            g.setStatus(GatepassStatus.OVERDUE);
            gatepassRepository.save(g);
            log.info("Marked gatepass {} as OVERDUE", g.getGatepassNumber());
        });
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    public GatepassResponse getGatepassResponse(Long id) {
        return toResponse(getGatepassById(id));
    }

    private Gatepass getGatepassById(Long id) {
        return gatepassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gatepass not found: " + id));
    }

    private String generateGatepassNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Use total count + a nanosecond-based suffix to avoid collisions on concurrent
        // requests or after deletions (pure count+1 can repeat).
        long count = gatepassRepository.count() + 1;
        int nanoSuffix = (int)(System.nanoTime() % 1000);
        return "GP-" + date + "-" + String.format("%04d", count) + String.format("%03d", nanoSuffix);
    }

    private void audit(Long userId, String email, String action, String entity, Long entityId, String details) {
        auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .userEmail(email)
                .action(action)
                .entityType(entity)
                .entityId(entityId)
                .details(details)
                .build());
    }

    public GatepassResponse toResponse(Gatepass g) {
        GatepassResponse r = new GatepassResponse();
        r.setId(g.getId());
        r.setGatepassNumber(g.getGatepassNumber());
        r.setStatus(g.getStatus());
        r.setDestination(g.getDestination());
        r.setReason(g.getReason());
        r.setOutDateTime(g.getOutDateTime());
        r.setExpectedReturnDateTime(g.getExpectedReturnDateTime());
        r.setActualReturnDateTime(g.getActualReturnDateTime());
        r.setParentName(g.getParentName());
        r.setParentPhone(g.getParentPhone());
        r.setParentRelation(g.getParentRelation());
        r.setUrgent(g.isUrgent());
        r.setAdditionalNotes(g.getAdditionalNotes());
        r.setQrCode(g.getQrCode());
        r.setQrCodeImage(g.getQrCodeImage());
        r.setCreatedAt(g.getCreatedAt());
        r.setUpdatedAt(g.getUpdatedAt());

        // Student info
        if (g.getStudent() != null) {
            r.setStudentId(g.getStudent().getId());
            r.setStudentName(g.getStudent().getName());
            r.setStudentRollNumber(g.getStudent().getRollNumber());
            r.setStudentRoom(g.getStudent().getRoomNumber());
            r.setStudentPhone(g.getStudent().getPhoneNumber());
            r.setStudentDepartment(g.getStudent().getDepartment());
        }

        if (g.getCoordinator() != null) {
            r.setCoordinatorName(g.getCoordinator().getName());
            r.setCoordinatorReviewedAt(g.getCoordinatorReviewedAt());
            r.setCoordinatorRemarks(g.getCoordinatorRemarks());
        }

        if (g.getWarden() != null) {
            r.setWardenName(g.getWarden().getName());
            r.setWardenReviewedAt(g.getWardenReviewedAt());
            r.setWardenRemarks(g.getWardenRemarks());
        }

        if (g.getExitGuard() != null) {
            r.setExitGuardName(g.getExitGuard().getName());
            r.setExitScannedAt(g.getExitScannedAt());
        }

        if (g.getReturnGuard() != null) {
            r.setReturnGuardName(g.getReturnGuard().getName());
            r.setReturnScannedAt(g.getReturnScannedAt());
        }

        return r;
    }
}