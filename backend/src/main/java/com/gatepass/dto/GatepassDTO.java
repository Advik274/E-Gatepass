package com.gatepass.dto;

import com.gatepass.enums.GatepassStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

public class GatepassDTO {

    @Data
    public static class CreateRequest {
        @NotBlank
        private String destination;
        @NotBlank
        private String reason;
        @NotNull
        private LocalDateTime outDateTime;
        @NotNull
        private LocalDateTime expectedReturnDateTime;
        private String parentName;
        private String parentPhone;
        private String parentRelation;
        private boolean urgent;
        private String additionalNotes;
    }

    @Data
    public static class ReviewRequest {
        @NotNull
        private boolean approved;
        private String remarks;
    }

    @Data
    public static class GatepassResponse {
        private Long id;
        private String gatepassNumber;
        private GatepassStatus status;
        private String destination;
        private String reason;
        private LocalDateTime outDateTime;
        private LocalDateTime expectedReturnDateTime;
        private LocalDateTime actualReturnDateTime;
        private String parentName;
        private String parentPhone;
        private String parentRelation;
        private boolean urgent;
        private String additionalNotes;

        // Student info
        private Long studentId;
        private String studentName;
        private String studentRollNumber;
        private String studentRoom;
        private String studentPhone;
        private String studentDepartment;

        // Coordinator info
        private String coordinatorName;
        private LocalDateTime coordinatorReviewedAt;
        private String coordinatorRemarks;

        // Warden info
        private String wardenName;
        private LocalDateTime wardenReviewedAt;
        private String wardenRemarks;

        // Security info
        private String exitGuardName;
        private LocalDateTime exitScannedAt;
        private String returnGuardName;
        private LocalDateTime returnScannedAt;

        // QR
        private String qrCode;
        private String qrCodeImage;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class ScanRequest {
        private String qrCode;
        private String gatepassNumber;
    }

    @Data
    public static class ScanResponse {
        private boolean valid;
        private String message;
        private String action;   // EXIT or RETURN
        private GatepassResponse gatepass;
    }

    @Data
    public static class StatsResponse {
        private long totalGatepasses;
        private long pending;
        private long coordinatorApproved;
        private long wardenApproved;
        private long active;
        private long completed;
        private long overdue;
        private long rejected;
        private long todayTotal;
        private long currentlyOutside;
    }
}