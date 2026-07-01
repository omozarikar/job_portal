package com.jobportal.controller;

import com.jobportal.dto.request.ApplicationRequest;
import com.jobportal.dto.request.UpdateApplicationStatusRequest;
import com.jobportal.dto.response.ApiResponse;
import com.jobportal.dto.response.ApplicationResponse;
import com.jobportal.dto.response.PagedResponse;
import com.jobportal.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Job application management")
@SecurityRequirement(name = "bearerAuth")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @Operation(summary = "Apply for a job (candidate only)")
    public ResponseEntity<ApiResponse<ApplicationResponse>> apply(
            @Valid @RequestBody ApplicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ApplicationResponse response = applicationService.applyForJob(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully", response));
    }

    @GetMapping("/my-applications")
    @Operation(summary = "Get current candidate's applications")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationResponse>>> myApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Applications fetched",
                applicationService.getMyApplications(userDetails.getUsername(), page, size)));
    }

    @DeleteMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw a job application (candidate only)")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        applicationService.withdrawApplication(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn", null));
    }

    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get all applications for a job (employer only)")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationResponse>>> getApplicationsForJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Applications fetched",
                applicationService.getApplicationsForJob(jobId, userDetails.getUsername(), page, size)));
    }

    @GetMapping("/employer")
    @Operation(summary = "Get all applications across employer's jobs")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationResponse>>> getEmployerApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Applications fetched",
                applicationService.getAllApplicationsByEmployer(userDetails.getUsername(), page, size)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update application status (employer only)")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateApplicationStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                applicationService.updateApplicationStatus(id, request, userDetails.getUsername())));
    }
}
