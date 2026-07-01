package com.jobportal.service;

import com.jobportal.dto.request.LoginRequest;
import com.jobportal.dto.request.RegisterRequest;
import com.jobportal.dto.response.AuthResponse;
import com.jobportal.dto.response.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser(String email);
    UserResponse updateProfile(String email, RegisterRequest request);
}
