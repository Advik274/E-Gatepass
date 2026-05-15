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
@RequestMapping("/warden")
@RequiredArgsConstructor
public class WardenController {

    private final GatepassService gatepassService;

    /** All coordinator-approved, awaiting warden final decision */
    @GetMapping("/pending")
    public ResponseEntity<List<GatepassResponse>> getPending() {
        return ResponseEntity.ok(gatepassService.getPendingForWarden());
    }

    /** Full view of all gatepasses */
    @GetMapping("/all")
    public ResponseEntity<List<GatepassResponse>> getAll() {
        return ResponseEntity.ok(gatepassService.getAllForWarden());
    }

    /** Currently outside */
    @GetMapping("/outside")
    public ResponseEntity<List<GatepassResponse>> getOutside() {
        return ResponseEntity.ok(gatepassService.getCurrentlyOutside());
    }

    /** Overdue students */
    @GetMapping("/overdue")
    public ResponseEntity<List<GatepassResponse>> getOverdue() {
        return ResponseEntity.ok(gatepassService.getOverdueGatepasses());
    }

    /** Get single gatepass detail */
    @GetMapping("/gatepass/{id}")
    public ResponseEntity<GatepassResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(gatepassService.getGatepassResponse(id));
    }

    /** Final approval or rejection */
    @PostMapping("/gatepass/{id}/review")
    public ResponseEntity<GatepassResponse> review(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gatepassService.wardenReview(id, request, userDetails.getUsername()));
    }

    /** Bulk approve */
    @PostMapping("/bulk-approve")
    public ResponseEntity<Void> bulkApprove(
            @RequestBody Map<String, List<Long>> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        gatepassService.bulkWardenApprove(body.get("ids"), userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    /** Dashboard stats */
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> stats() {
        return ResponseEntity.ok(gatepassService.getStats());
    }
}