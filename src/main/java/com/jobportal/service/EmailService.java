package com.jobportal.service;

public interface EmailService {
    void sendApplicationConfirmation(String candidateEmail, String candidateName, String jobTitle, String company);
    void sendApplicationStatusUpdate(String candidateEmail, String candidateName, String jobTitle, String status, String notes);
    void sendNewApplicationNotification(String employerEmail, String employerName, String candidateName, String jobTitle);
    void sendWelcomeEmail(String userEmail, String userName);
}
