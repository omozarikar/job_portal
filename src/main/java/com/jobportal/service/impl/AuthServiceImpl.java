package com.jobportal.service.impl;

import com.jobportal.dto.request.LoginRequest;
import com.jobportal.dto.request.RegisterRequest;
import com.jobportal.dto.response.AuthResponse;
import com.jobportal.dto.response.UserResponse;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.UserRepository;
import com.jobportal.security.JwtTokenProvider;
import com.jobportal.service.AuthService;
import com.jobportal.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .location(request.getLocation())
                .skills(request.getSkills())
                .experience(request.getExperience())
                .education(request.getEducation())
                .companyName(request.getCompanyName())
                .companyWebsite(request.getCompanyWebsite())
                .companyDescription(request.getCompanyDescription())
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("New user registered: {} with role {}", user.getEmail(), user.getRole());

        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

        String token = jwtTokenProvider.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, RegisterRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setLocation(request.getLocation());
        user.setSkills(request.getSkills());
        user.setExperience(request.getExperience());
        user.setEducation(request.getEducation());
        user.setCompanyName(request.getCompanyName());
        user.setCompanyWebsite(request.getCompanyWebsite());
        user.setCompanyDescription(request.getCompanyDescription());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .location(user.getLocation())
                .role(user.getRole())
                .skills(user.getSkills())
                .experience(user.getExperience())
                .education(user.getEducation())
                .companyName(user.getCompanyName())
                .companyWebsite(user.getCompanyWebsite())
                .companyDescription(user.getCompanyDescription())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
