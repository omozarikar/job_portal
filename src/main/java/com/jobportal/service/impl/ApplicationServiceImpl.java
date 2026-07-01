package com.jobportal.service.impl;

import com.jobportal.dto.request.ApplicationRequest;
import com.jobportal.dto.request.UpdateApplicationStatusRequest;
import com.jobportal.dto.response.ApplicationResponse;
import com.jobportal.dto.response.PagedResponse;
import com.jobportal.entity.Job;
import com.jobportal.entity.JobApplication;
import com.jobportal.entity.Resume;
import com.jobportal.entity.User;
import com.jobportal.enums.ApplicationStatus;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.exception.UnauthorizedException;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.ResumeRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public ApplicationResponse applyForJob(ApplicationRequest request, String candidateEmail) {
        User candidate = getUserByEmail(candidateEmail);
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job", request.getJobId()));

        if (!job.isActive()) {
            throw new BadRequestException("This job is no longer accepting applications");
        }

        if (applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), job.getId())) {
            throw new BadRequestException("You have already applied for this job");
        }

        String resumePath = null;
        if (request.getResumeId() != null) {
            Resume resume = resumeRepository.findById(request.getResumeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Resume", request.getResumeId()));
            if (!resume.getUser().getId().equals(candidate.getId())) {
                throw new UnauthorizedException("You don't own this resume");
            }
            resumePath = resume.getFilePath();
        }

        JobApplication application = JobApplication.builder()
                .candidate(candidate)
                .job(job)
                .status(ApplicationStatus.APPLIED)   // explicit — Lombok @Builder ignores field defaults
                .coverLetter(request.getCoverLetter())
                .resumePath(resumePath)
                .build();

        application = applicationRepository.save(application);
        log.info("Application submitted: candidate={}, job={}", candidateEmail, job.getId());

        // Send async email notifications
        sendApplicationEmails(candidate, job);

        return mapToApplicationResponse(application);
    }

    @Async
    protected void sendApplicationEmails(User candidate, Job job) {
        try {
            emailService.sendApplicationConfirmation(
                    candidate.getEmail(), candidate.getFullName(),
                    job.getTitle(), job.getCompany());
            emailService.sendNewApplicationNotification(
                    job.getPostedBy().getEmail(), job.getPostedBy().getFullName(),
                    candidate.getFullName(), job.getTitle());
        } catch (Exception e) {
            log.error("Failed to send application email notifications: {}", e.getMessage());
        }
    }

    @Override
    public PagedResponse<ApplicationResponse> getMyApplications(String candidateEmail, int page, int size) {
        User candidate = getUserByEmail(candidateEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        Page<JobApplication> applications = applicationRepository.findByCandidateId(candidate.getId(), pageable);
        return buildPagedResponse(applications);
    }

    @Override
    public PagedResponse<ApplicationResponse> getApplicationsForJob(Long jobId, String employerEmail, int page, int size) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        if (!job.getPostedBy().getEmail().equals(employerEmail)) {
            throw new UnauthorizedException("You don't have access to these applications");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        Page<JobApplication> applications = applicationRepository.findByJobId(jobId, pageable);
        return buildPagedResponse(applications);
    }

    @Override
    public PagedResponse<ApplicationResponse> getAllApplicationsByEmployer(String employerEmail, int page, int size) {
        User employer = getUserByEmail(employerEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        Page<JobApplication> applications = applicationRepository.findByJobPostedById(employer.getId(), pageable);
        return buildPagedResponse(applications);
    }

    @Override
    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId,
                                                       UpdateApplicationStatusRequest request,
                                                       String employerEmail) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", applicationId));

        if (!application.getJob().getPostedBy().getEmail().equals(employerEmail)) {
            throw new UnauthorizedException("You don't have access to this application");
        }

        application.setStatus(request.getStatus());
        if (request.getEmployerNotes() != null) {
            application.setEmployerNotes(request.getEmployerNotes());
        }

        application = applicationRepository.save(application);
        log.info("Application {} status updated to {}", applicationId, request.getStatus());

        // Notify candidate
        try {
            emailService.sendApplicationStatusUpdate(
                    application.getCandidate().getEmail(),
                    application.getCandidate().getFullName(),
                    application.getJob().getTitle(),
                    request.getStatus().name(),
                    request.getEmployerNotes()
            );
        } catch (Exception e) {
            log.error("Failed to send status update email: {}", e.getMessage());
        }

        return mapToApplicationResponse(application);
    }

    @Override
    @Transactional
    public void withdrawApplication(Long applicationId, String candidateEmail) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", applicationId));

        if (!application.getCandidate().getEmail().equals(candidateEmail)) {
            throw new UnauthorizedException("You don't have access to this application");
        }

        applicationRepository.delete(application);
        log.info("Application {} withdrawn by {}", applicationId, candidateEmail);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private ApplicationResponse mapToApplicationResponse(JobApplication app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .company(app.getJob().getCompany())
                .candidateId(app.getCandidate().getId())
                .candidateName(app.getCandidate().getFullName())
                .candidateEmail(app.getCandidate().getEmail())
                .status(app.getStatus())
                .coverLetter(app.getCoverLetter())
                .resumePath(app.getResumePath())
                .employerNotes(app.getEmployerNotes())
                .appliedAt(app.getAppliedAt())
                .build();
    }

    private PagedResponse<ApplicationResponse> buildPagedResponse(Page<JobApplication> page) {
        return PagedResponse.<ApplicationResponse>builder()
                .content(page.getContent().stream().map(this::mapToApplicationResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
