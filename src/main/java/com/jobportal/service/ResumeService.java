package com.jobportal.service;

import com.jobportal.entity.Resume;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService {
    Resume uploadResume(MultipartFile file, String userEmail);
    List<Resume> getMyResumes(String userEmail);
    void deleteResume(Long resumeId, String userEmail);
    void setPrimaryResume(Long resumeId, String userEmail);
    byte[] downloadResume(Long resumeId, String requesterEmail);
}
