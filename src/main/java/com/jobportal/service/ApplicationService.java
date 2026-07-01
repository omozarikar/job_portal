package com.jobportal.service;

import com.jobportal.dto.request.ApplicationRequest;
import com.jobportal.dto.request.UpdateApplicationStatusRequest;
import com.jobportal.dto.response.ApplicationResponse;
import com.jobportal.dto.response.PagedResponse;

public interface ApplicationService {
    ApplicationResponse applyForJob(ApplicationRequest request, String candidateEmail);
    PagedResponse<ApplicationResponse> getMyApplications(String candidateEmail, int page, int size);
    PagedResponse<ApplicationResponse> getApplicationsForJob(Long jobId, String employerEmail, int page, int size);
    PagedResponse<ApplicationResponse> getAllApplicationsByEmployer(String employerEmail, int page, int size);
    ApplicationResponse updateApplicationStatus(Long applicationId, UpdateApplicationStatusRequest request, String employerEmail);
    void withdrawApplication(Long applicationId, String candidateEmail);
}
