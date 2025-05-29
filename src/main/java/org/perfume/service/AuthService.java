package org.perfume.service;

import org.perfume.model.dto.request.LoginRequest;
import org.perfume.model.dto.request.RegisterRequest;
import org.perfume.model.dto.response.AuthResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse register(RegisterRequest registerRequest);
    AuthResponse handleOAuth2User(OAuth2User oAuth2User);
}