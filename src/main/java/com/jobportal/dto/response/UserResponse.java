package com.jobportal.dto.response;

import com.jobportal.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String location;
    private Role role;
    private String skills;
    private String experience;
    private String education;
    private String companyName;
    private String companyWebsite;
    private String companyDescription;
    private LocalDateTime createdAt;
}
