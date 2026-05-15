package com.gatepass.entity;

import com.gatepass.enums.GatepassStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "gatepasses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gatepass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String gatepassNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User student;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime outDateTime;

    @Column(nullable = false)
    private LocalDateTime expectedReturnDateTime;

    private LocalDateTime actualReturnDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GatepassStatus status = GatepassStatus.PENDING;

    // Parent contact
    private String parentName;
    private String parentPhone;
    private String parentRelation;

    // Coordinator review
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User coordinator;

    private LocalDateTime coordinatorReviewedAt;
    private String coordinatorRemarks;

    // Warden review
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warden_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User warden;

    private LocalDateTime wardenReviewedAt;
    private String wardenRemarks;

    // Security scans
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exit_guard_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User exitGuard;

    private LocalDateTime exitScannedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_guard_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User returnGuard;

    private LocalDateTime returnScannedAt;

    // QR Code
    @Column(length = 512)
    private String qrCode;           // SHA256 hash

    @Column(length = 2048)
    private String qrCodeImage;      // Base64 encoded PNG

    // Urgency
    @Builder.Default
    private boolean urgent = false;

    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}