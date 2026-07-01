package com.jobportal.service.impl;

import com.jobportal.entity.Resume;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.exception.UnauthorizedException;
import com.jobportal.repository.ResumeRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    @Override
    @Transactional
    public Resume uploadResume(MultipartFile file, String userEmail) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BadRequestException("Only PDF, DOC, and DOCX files are allowed");
        }

        User user = getUserByEmail(userEmail);

        if (resumeRepository.countByUserId(user.getId()) >= 5) {
            throw new BadRequestException("Maximum 5 resumes allowed per user");
        }

        try {
            Path uploadPath = Paths.get(uploadDir).resolve(String.valueOf(user.getId()));
            Files.createDirectories(uploadPath);

            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;
            Path filePath = uploadPath.resolve(uniqueFileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            boolean isPrimary = resumeRepository.countByUserId(user.getId()) == 0;

            Resume resume = Resume.builder()
                    .user(user)
                    .fileName(originalFileName)
                    .filePath(filePath.toString())
                    .fileType(contentType)
                    .fileSize(file.getSize())
                    .isPrimary(isPrimary)
                    .build();

            log.info("Resume uploaded for user: {}", userEmail);
            return resumeRepository.save(resume);

        } catch (IOException e) {
            log.error("Failed to store resume file: {}", e.getMessage());
            throw new BadRequestException("Failed to upload resume. Please try again.");
        }
    }

    @Override
    public List<Resume> getMyResumes(String userEmail) {
        User user = getUserByEmail(userEmail);
        return resumeRepository.findByUserId(user.getId());
    }

    @Override
    @Transactional
    public void deleteResume(Long resumeId, String userEmail) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", resumeId));
        verifyResumeOwnership(resume, userEmail);

        try {
            Files.deleteIfExists(Paths.get(resume.getFilePath()));
        } catch (IOException e) {
            log.warn("Could not delete physical file: {}", resume.getFilePath());
        }

        resumeRepository.delete(resume);
    }

    @Override
    @Transactional
    public void setPrimaryResume(Long resumeId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", resumeId));
        verifyResumeOwnership(resume, userEmail);

        // Clear existing primary
        resumeRepository.findByUserIdAndIsPrimaryTrue(user.getId())
                .ifPresent(r -> { r.setPrimary(false); resumeRepository.save(r); });

        resume.setPrimary(true);
        resumeRepository.save(resume);
    }

    @Override
    public byte[] downloadResume(Long resumeId, String requesterEmail) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", resumeId));

        try {
            return Files.readAllBytes(Paths.get(resume.getFilePath()));
        } catch (IOException e) {
            throw new BadRequestException("Resume file not found or could not be read");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private void verifyResumeOwnership(Resume resume, String userEmail) {
        if (!resume.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("You don't own this resume");
        }
    }
}
