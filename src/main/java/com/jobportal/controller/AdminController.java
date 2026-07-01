package com.jobportal.controller;

import com.jobportal.dto.response.ApiResponse;
import com.jobportal.dto.response.DashboardStats;
import com.jobportal.dto.response.JobResponse;
import com.jobportal.dto.response.PagedResponse;
import com.jobportal.dto.response.UserResponse;
import com.jobportal.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin dashboard and management (admin role required)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get platform-wide statistics")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats", adminService.getDashboardStats()));
    }

    @GetMapping("/users")
    @Operation(summary = "List all users (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Users fetched", adminService.getAllUsers(page, size)));
    }

    @PatchMapping("/users/{id}/toggle-status")
    @Operation(summary = "Enable or disable a user account")
    public ResponseEntity<ApiResponse<Void>> toggleUser(@PathVariable Long id) {
        adminService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status updated", null));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Permanently delete a user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }

    @GetMapping("/jobs")
    @Operation(summary = "List all jobs including inactive (admin view)")
    public ResponseEntity<ApiResponse<PagedResponse<JobResponse>>> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Jobs fetched", adminService.getAllJobsAdmin(page, size)));
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Force delete any job")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id) {
        adminService.deleteJobAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Job deleted", null));
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Change a user's role (admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        UserResponse response = adminService.updateUserRole(id, request.getRole());
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", response));
    }
}
