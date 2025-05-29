package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.User;
import org.perfume.domain.repo.UserDao;
import org.perfume.exception.AlreadyExistsException;
import org.perfume.mapper.UserMapper;
import org.perfume.model.dto.request.LoginRequest;
import org.perfume.model.dto.request.RegisterRequest;
import org.perfume.model.dto.response.AuthResponse;
import org.perfume.model.enums.UserRole;
import org.perfume.security.JwtTokenProvider;
import org.perfume.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        User user = userDao.findByEmail(loginRequest.getEmail()).orElseThrow();

        return new AuthResponse(jwt, userMapper.toDto(user));
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userDao.existsByEmail(registerRequest.getEmail())) {
            throw new AlreadyExistsException("Email is already taken");
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setRole(UserRole.USER);

        User savedUser = userDao.save(user);
        String jwt = tokenProvider.generateToken(savedUser);

        return new AuthResponse(jwt, userMapper.toDto(savedUser));
    }

    @Override
    @Transactional
    public AuthResponse handleOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userDao.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                    newUser.setRole(UserRole.USER);
                    newUser.setGoogleUser(true);
                    return userDao.save(newUser);
                });

        String jwt = tokenProvider.generateToken(user);
        return new AuthResponse(jwt, userMapper.toDto(user));
    }
}