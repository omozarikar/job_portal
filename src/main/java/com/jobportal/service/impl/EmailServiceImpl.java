package com.jobportal.service.impl;

import com.jobportal.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendApplicationConfirmation(String candidateEmail, String candidateName,
                                             String jobTitle, String company) {
        String subject = "Application Received – " + jobTitle + " at " + company;
        String body = String.format("""
                Hi %s,
                
                Your application for "%s" at %s has been successfully submitted.
                
                We'll notify you when the employer reviews your application.
                
                Best regards,
                Job Portal Team
                """, candidateName, jobTitle, company);
        sendEmail(candidateEmail, subject, body);
    }

    @Override
    @Async
    public void sendApplicationStatusUpdate(String candidateEmail, String candidateName,
                                             String jobTitle, String status, String notes) {
        String subject = "Application Update – " + jobTitle;
        String body = String.format("""
                Hi %s,
                
                Your application for "%s" has been updated.
                
                New Status: %s
                %s
                
                Log in to your account for more details.
                
                Best regards,
                Job Portal Team
                """, candidateName, jobTitle, status,
                notes != null ? "Notes from employer: " + notes : "");
        sendEmail(candidateEmail, subject, body);
    }

    @Override
    @Async
    public void sendNewApplicationNotification(String employerEmail, String employerName,
                                                String candidateName, String jobTitle) {
        String subject = "New Application – " + jobTitle;
        String body = String.format("""
                Hi %s,
                
                %s has applied for your job posting "%s".
                
                Log in to your employer dashboard to review the application.
                
                Best regards,
                Job Portal Team
                """, employerName, candidateName, jobTitle);
        sendEmail(employerEmail, subject, body);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String userEmail, String userName) {
        String subject = "Welcome to Job Portal!";
        String body = String.format("""
                Hi %s,
                
                Welcome to Job Portal! Your account has been successfully created.
                
                Start exploring thousands of opportunities today.
                
                Best regards,
                Job Portal Team
                """, userName);
        sendEmail(userEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
