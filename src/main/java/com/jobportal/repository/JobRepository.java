package com.jobportal.repository;

import com.jobportal.entity.Job;
import com.jobportal.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByActiveTrue(Pageable pageable);

    Page<Job> findByPostedByIdAndActiveTrue(Long employerId, Pageable pageable);

    @Query("""
        SELECT j FROM Job j
        WHERE j.active = true
        AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                              OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                              OR LOWER(j.skillsRequired) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
        AND (:jobType IS NULL OR j.jobType = :jobType)
        AND (:company IS NULL OR LOWER(j.company) LIKE LOWER(CONCAT('%', :company, '%')))
    """)
    Page<Job> searchJobs(
        @Param("keyword") String keyword,
        @Param("location") String location,
        @Param("jobType") JobType jobType,
        @Param("company") String company,
        Pageable pageable
    );

    long countByActiveTrue();
}
