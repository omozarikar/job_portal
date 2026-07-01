package com.jobportal.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class DashboardStats {
    private long totalUsers;
    private long totalCandidates;
    private long totalEmployers;
    private long totalJobs;
    private long activeJobs;
    private long totalApplications;
    private long pendingApplications;
    private long hiredCandidates;
}
