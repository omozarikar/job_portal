package com.jobportal.service;

import com.jobportal.dto.response.DashboardStats;
import com.jobportal.dto.response.JobResponse;
import com.jobportal.dto.response.PagedResponse;
import com.jobportal.dto.response.UserResponse;

public interface AdminService {
    DashboardStats getDashboardStats();
    PagedResponse<UserResponse> getAllUsers(int page, int size);
    void toggleUserStatus(Long userId);
    void deleteUser(Long userId);
    PagedResponse<JobResponse> getAllJobsAdmin(int page, int size);
    void deleteJobAdmin(Long jobId);
    UserResponse updateUserRole(Long userId, com.jobportal.enums.Role newRole);
}
