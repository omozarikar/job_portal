package com.jobportal.repository;

import com.jobportal.entity.JobApplication;
import com.jobportal.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    Page<JobApplication> findByCandidateId(Long candidateId, Pageable pageable);
    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);
    Page<JobApplication> findByJobPostedById(Long employerId, Pageable pageable);
    Optional<JobApplication> findByCandidateIdAndJobId(Long candidateId, Long jobId);
    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);
    long countByStatus(ApplicationStatus status);
}
