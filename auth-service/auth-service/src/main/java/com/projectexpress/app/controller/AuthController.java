package com.projectexpress.app.controller;

import com.projectexpress.app.dto.*;
import com.projectexpress.app.entity.User;
import com.projectexpress.app.service.AuthService;
import com.projectexpress.app.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest){
        User user = authService.register(registerRequest);

        RegisterResponse registerResponse = new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){

        LoginResponse loginResponse = authService.login(loginRequest);

        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    //refresh token end point to generate new access token
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest){
        RefreshTokenResponse refreshTokenResponse = refreshTokenService.refreshAccessToken(refreshTokenRequest.getRefreshToken());

        return ResponseEntity.ok(refreshTokenResponse);
    }

    @PostMapping("/logout")
    public  ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest){
        refreshTokenService.logout(refreshTokenRequest.getRefreshToken());

        return ResponseEntity.ok("Logout successful");
    }


    //for delete only for ADMIN
    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity updateUser(@Valid @RequestBody UpdateRequest updateRequest) {
        authService.updateUser(updateRequest);
        return ResponseEntity.ok("User password and role updated successfully");
    }




}
