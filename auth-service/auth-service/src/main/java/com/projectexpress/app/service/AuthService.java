package com.projectexpress.app.service;

import com.projectexpress.app.dto.LoginRequest;
import com.projectexpress.app.dto.LoginResponse;
import com.projectexpress.app.dto.RegisterRequest;
import com.projectexpress.app.dto.UpdateRequest;
import com.projectexpress.app.entity.RefreshToken;
import com.projectexpress.app.entity.User;
import com.projectexpress.app.event.UserRegisteredEvent;
import com.projectexpress.app.event.UserRegisteredEventProducer;
import com.projectexpress.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRegisteredEventProducer userRegisteredEventProducer;


    //register
    public User register(RegisterRequest registerRequest) {

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role("ROLE_USER")
                .build();

        // 1. Save user in auth_db
        User savedUser = userRepository.save(user);

        // 2. Create registration event
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .build();

        // 3. Publish event to Kafka
        userRegisteredEventProducer.sendUserRegisteredEvent(event);

        return savedUser;
    }

    //login
    public LoginResponse login(LoginRequest loginRequest) {

        User existingUser = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        //generate access token
        String accessToken = jwtService.generateToken(existingUser.getId(), existingUser.getEmail(), existingUser.getRole());

        //generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(existingUser);

        return new LoginResponse(accessToken, refreshToken.getToken());

    }

    //update
    public void updateUser(UpdateRequest request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getId()));

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);
    }
}