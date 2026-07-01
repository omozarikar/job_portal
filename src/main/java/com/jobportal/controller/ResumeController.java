package com.jobportal.controller;

import com.jobportal.dto.response.ApiResponse;
import com.jobportal.entity.Resume;
import com.jobportal.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Tag(name = "Resumes", description = "Resume upload and management")
@SecurityRequirement(name = "bearerAuth")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a resume (PDF, DOC, DOCX — max 5MB)")
    public ResponseEntity<ApiResponse<Resume>> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        Resume resume = resumeService.uploadResume(file, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resume uploaded successfully", resume));
    }

    @GetMapping
    @Operation(summary = "Get all resumes of current user")
    public ResponseEntity<ApiResponse<List<Resume>>> getMyResumes(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Resume> resumes = resumeService.getMyResumes(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Resumes fetched", resumes));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resume")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        resumeService.deleteResume(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Resume deleted", null));
    }

    @PatchMapping("/{id}/set-primary")
    @Operation(summary = "Set a resume as the primary resume")
    public ResponseEntity<ApiResponse<Void>> setPrimary(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        resumeService.setPrimaryResume(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Primary resume updated", null));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download a resume file")
    public ResponseEntity<byte[]> download(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        byte[] data = resumeService.downloadResume(id, userDetails.getUsername());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("resume_" + id + ".pdf").build());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
