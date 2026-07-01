package com.jobportal.service.impl;

import com.jobportal.dto.response.DashboardStats;
import com.jobportal.dto.response.JobResponse;
import com.jobportal.dto.response.PagedResponse;
import com.jobportal.dto.response.UserResponse;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.enums.ApplicationStatus;
import com.jobportal.enums.Role;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;

    @Override
    public DashboardStats getDashboardStats() {
        return DashboardStats.builder()
                .totalUsers(userRepository.count())
                .totalCandidates(userRepository.countByRole(Role.ROLE_CANDIDATE))
                .totalEmployers(userRepository.countByRole(Role.ROLE_EMPLOYER))
                .totalJobs(jobRepository.count())
                .activeJobs(jobRepository.countByActiveTrue())
                .totalApplications(applicationRepository.count())
                .pendingApplications(applicationRepository.countByStatus(ApplicationStatus.APPLIED))
                .hiredCandidates(applicationRepository.countByStatus(ApplicationStatus.HIRED))
                .build();
    }

    @Override
    public PagedResponse<UserResponse> getAllUsers(int page, int size) {
        Page<User> users = userRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PagedResponse.<UserResponse>builder()
                .content(users.getContent().stream().map(this::mapToUserResponse).toList())
                .page(users.getNumber()).size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .last(users.isLast()).build();
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        log.info("User {} status toggled to: {}", userId, user.isEnabled());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        userRepository.delete(user);
        log.info("User {} deleted by admin", userId);
    }

    @Override
    public PagedResponse<JobResponse> getAllJobsAdmin(int page, int size) {
        Page<Job> jobs = jobRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PagedResponse.<JobResponse>builder()
                .content(jobs.getContent().stream().map(this::mapToJobResponse).toList())
                .page(jobs.getNumber()).size(jobs.getSize())
                .totalElements(jobs.getTotalElements())
                .totalPages(jobs.getTotalPages())
                .last(jobs.isLast()).build();
    }

    @Override
    @Transactional
    public void deleteJobAdmin(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        jobRepository.delete(job);
        log.info("Job {} deleted by admin", jobId);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId()).fullName(user.getFullName())
                .email(user.getEmail()).phone(user.getPhone())
                .location(user.getLocation()).role(user.getRole())
                .skills(user.getSkills()).experience(user.getExperience())
                .companyName(user.getCompanyName()).createdAt(user.getCreatedAt()).build();
    }

    private JobResponse mapToJobResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId()).title(job.getTitle())
                .description(job.getDescription()).company(job.getCompany())
                .location(job.getLocation()).jobType(job.getJobType())
                .active(job.isActive()).postedByName(job.getPostedBy().getFullName())
                .postedById(job.getPostedBy().getId()).createdAt(job.getCreatedAt()).build();
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long userId, com.jobportal.enums.Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.jobportal.exception.ResourceNotFoundException("User", userId));
        user.setRole(newRole);
        userRepository.save(user);
        log.info("User {} role changed to {}", userId, newRole);
        return mapToUserResponse(user);
    }
}
