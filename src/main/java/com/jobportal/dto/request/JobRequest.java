package com.jobportal.dto.request;

import com.jobportal.enums.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class JobRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Company is required")
    private String company;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    private BigDecimal salaryMin;
    private BigDecimal salaryMax;

    @NotBlank(message = "Experience required is mandatory")
    private String experienceRequired;

    private String skillsRequired;
    private String qualifications;
    private LocalDate applicationDeadline;
}
