package com.jobportal.dto.response;

import com.jobportal.enums.JobType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String company;
    private String location;
    private JobType jobType;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String experienceRequired;
    private String skillsRequired;
    private String qualifications;
    private LocalDate applicationDeadline;
    private boolean active;
    private String postedByName;
    private Long postedById;
    private int applicationCount;
    private LocalDateTime createdAt;
}
