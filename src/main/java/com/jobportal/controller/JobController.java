package com.jobportal.controller;

import com.jobportal.dto.request.JobRequest;
import com.jobportal.dto.response.ApiResponse;
import com.jobportal.dto.response.JobResponse;
import com.jobportal.dto.response.PagedResponse;
import com.jobportal.enums.JobType;
import com.jobportal.service.JobService;
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
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job posting, search, and management")
public class JobController {

    private final JobService jobService;

    @GetMapping
    @Operation(summary = "Get all active jobs (public)")
    public ResponseEntity<ApiResponse<PagedResponse<JobResponse>>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Jobs fetched", jobService.getAllJobs(page, size)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search jobs with filters (public)")
    public ResponseEntity<ApiResponse<PagedResponse<JobResponse>>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) String company,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Search results",
                jobService.searchJobs(keyword, location, jobType, company, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by ID (public)")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Job fetched", jobService.getJobById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new job (employer only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @Valid @RequestBody JobRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        JobResponse response = jobService.createJob(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a job (employer only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Job updated",
                jobService.updateJob(id, request, userDetails.getUsername())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a job (employer only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        jobService.deleteJob(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job deleted", null));
    }

    @GetMapping("/my-jobs")
    @Operation(summary = "Get employer's posted jobs", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PagedResponse<JobResponse>>> getMyJobs(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("My jobs",
                jobService.getJobsByEmployer(userDetails.getUsername(), page, size)));
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle job active/inactive (employer only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> toggleStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        jobService.toggleJobStatus(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Job status toggled", null));
    }
}
