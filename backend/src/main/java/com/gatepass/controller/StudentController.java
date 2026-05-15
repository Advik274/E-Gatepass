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

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final GatepassService gatepassService;

    /** Apply for a new gatepass */
    @PostMapping("/gatepass")
    public ResponseEntity<GatepassResponse> apply(
            @Valid @RequestBody CreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gatepassService.createGatepass(request, userDetails.getUsername()));
    }

    /** Get all my gatepasses */
    @GetMapping("/gatepasses")
    public ResponseEntity<List<GatepassResponse>> myGatepasses(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gatepassService.getStudentGatepasses(userDetails.getUsername()));
    }

    /** Get single gatepass by id */
    @GetMapping("/gatepass/{id}")
    public ResponseEntity<GatepassResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(gatepassService.getGatepassResponse(id));
    }

    /** Cancel a pending gatepass */
    @DeleteMapping("/gatepass/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        gatepassService.cancelGatepass(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}