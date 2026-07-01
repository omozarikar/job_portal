package com.jobportal.service.impl;

import com.jobportal.dto.request.JobRequest;
import com.jobportal.dto.response.JobResponse;
import com.jobportal.dto.response.PagedResponse;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.enums.JobType;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.exception.UnauthorizedException;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository applicationRepository;

    @Override
    @Transactional
    public JobResponse createJob(JobRequest request, String employerEmail) {
        User employer = getUserByEmail(employerEmail);
        Job job = buildJobFromRequest(request, employer);
        Job saved = jobRepository.save(job);
        log.info("Job created: '{}' by employer: {}", saved.getTitle(), employerEmail);
        return mapToJobResponse(saved);
    }

    @Override
    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request, String employerEmail) {
        Job job = getJobAndVerifyOwnership(jobId, employerEmail);
        updateJobFromRequest(job, request);
        return mapToJobResponse(jobRepository.save(job));
    }

    @Override
    @Transactional
    public void deleteJob(Long jobId, String employerEmail) {
        Job job = getJobAndVerifyOwnership(jobId, employerEmail);
        jobRepository.delete(job);
        log.info("Job deleted: {} by employer: {}", jobId, employerEmail);
    }

    @Override
    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        return mapToJobResponse(job);
    }

    @Override
    public PagedResponse<JobResponse> getAllJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Job> jobs = jobRepository.findByActiveTrue(pageable);
        return buildPagedResponse(jobs);
    }

    @Override
    public PagedResponse<JobResponse> searchJobs(String keyword, String location,
                                                   JobType jobType, String company,
                                                   int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Job> jobs = jobRepository.searchJobs(
                isBlank(keyword) ? null : keyword,
                isBlank(location) ? null : location,
                jobType,
                isBlank(company) ? null : company,
                pageable
        );
        return buildPagedResponse(jobs);
    }

    @Override
    public PagedResponse<JobResponse> getJobsByEmployer(String employerEmail, int page, int size) {
        User employer = getUserByEmail(employerEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Job> jobs = jobRepository.findByPostedByIdAndActiveTrue(employer.getId(), pageable);
        return buildPagedResponse(jobs);
    }

    @Override
    @Transactional
    public void toggleJobStatus(Long jobId, String employerEmail) {
        Job job = getJobAndVerifyOwnership(jobId, employerEmail);
        job.setActive(!job.isActive());
        jobRepository.save(job);
    }

    // --- Helpers ---
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Job getJobAndVerifyOwnership(Long jobId, String employerEmail) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        if (!job.getPostedBy().getEmail().equals(employerEmail)) {
            throw new UnauthorizedException("You are not authorized to modify this job");
        }
        return job;
    }

    private Job buildJobFromRequest(JobRequest request, User employer) {
        return Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .company(request.getCompany())
                .location(request.getLocation())
                .jobType(request.getJobType())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .experienceRequired(request.getExperienceRequired())
                .skillsRequired(request.getSkillsRequired())
                .qualifications(request.getQualifications())
                .applicationDeadline(request.getApplicationDeadline())
                .active(true)
                .postedBy(employer)
                .build();
    }

    private void updateJobFromRequest(Job job, JobRequest request) {
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCompany(request.getCompany());
        job.setLocation(request.getLocation());
        job.setJobType(request.getJobType());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setExperienceRequired(request.getExperienceRequired());
        job.setSkillsRequired(request.getSkillsRequired());
        job.setQualifications(request.getQualifications());
        job.setApplicationDeadline(request.getApplicationDeadline());
    }

    private JobResponse mapToJobResponse(Job job) {
        int appCount = job.getApplications() != null ? job.getApplications().size() : 0;
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .company(job.getCompany())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .experienceRequired(job.getExperienceRequired())
                .skillsRequired(job.getSkillsRequired())
                .qualifications(job.getQualifications())
                .applicationDeadline(job.getApplicationDeadline())
                .active(job.isActive())
                .postedByName(job.getPostedBy().getFullName())
                .postedById(job.getPostedBy().getId())
                .applicationCount(appCount)
                .createdAt(job.getCreatedAt())
                .build();
    }

    private PagedResponse<JobResponse> buildPagedResponse(Page<Job> page) {
        return PagedResponse.<JobResponse>builder()
                .content(page.getContent().stream().map(this::mapToJobResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
