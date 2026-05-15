package com.gatepass.controller;

import com.gatepass.dto.GatepassDTO.*;
import com.gatepass.service.GatepassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coordinator")
@RequiredArgsConstructor
public class CoordinatorController {

    private final GatepassService gatepassService;

    /** Get all pending gatepasses for coordinator review */
    @GetMapping("/pending")
    public ResponseEntity<List<GatepassResponse>> getPending() {
        return ResponseEntity.ok(gatepassService.getPendingForCoordinator());
    }

    /** Get single gatepass detail */
    @GetMapping("/gatepass/{id}")
    public ResponseEntity<GatepassResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(gatepassService.getGatepassResponse(id));
    }

    /** Approve or reject a gatepass */
    @PostMapping("/gatepass/{id}/review")
    public ResponseEntity<GatepassResponse> review(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gatepassService.coordinatorReview(id, request, userDetails.getUsername()));
    }

    /** Bulk approve gatepasses */
    @PostMapping("/bulk-approve")
    public ResponseEntity<Void> bulkApprove(
            @RequestBody Map<String, List<Long>> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        gatepassService.bulkCoordinatorApprove(body.get("ids"), userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
