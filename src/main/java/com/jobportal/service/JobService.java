package com.jobportal.service;

import com.jobportal.dto.request.JobRequest;
import com.jobportal.dto.response.JobResponse;
import com.jobportal.dto.response.PagedResponse;
import com.jobportal.enums.JobType;

public interface JobService {
    JobResponse createJob(JobRequest request, String employerEmail);
    JobResponse updateJob(Long jobId, JobRequest request, String employerEmail);
    void deleteJob(Long jobId, String employerEmail);
    JobResponse getJobById(Long jobId);
    PagedResponse<JobResponse> getAllJobs(int page, int size);
    PagedResponse<JobResponse> searchJobs(String keyword, String location, JobType jobType, String company, int page, int size);
    PagedResponse<JobResponse> getJobsByEmployer(String employerEmail, int page, int size);
    void toggleJobStatus(Long jobId, String employerEmail);
}
