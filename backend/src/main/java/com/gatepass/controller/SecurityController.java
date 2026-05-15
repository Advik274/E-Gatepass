package com.gatepass.controller;

import com.gatepass.dto.GatepassDTO.*;
import com.gatepass.service.GatepassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/security")
@RequiredArgsConstructor
public class SecurityController {

    private final GatepassService gatepassService;

    /** Scan QR code or enter manual gatepass number */
    @PostMapping("/scan")
    public ResponseEntity<ScanResponse> scan(
            @RequestBody ScanRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gatepassService.scanGatepass(request, userDetails.getUsername()));
    }

    /** View who is currently outside */
    @GetMapping("/outside")
    public ResponseEntity<List<GatepassResponse>> currentlyOutside() {
        return ResponseEntity.ok(gatepassService.getCurrentlyOutside());
    }

    /** View overdue students */
    @GetMapping("/overdue")
    public ResponseEntity<List<GatepassResponse>> overdue() {
        return ResponseEntity.ok(gatepassService.getOverdueGatepasses());
    }
}