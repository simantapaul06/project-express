package com.projectexpress.app.controller;

import com.projectexpress.app.dto.RegisterResponse;
import com.projectexpress.app.entity.User;
import com.projectexpress.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    private final UserRepository userRepository;
    @GetMapping("/me")
    public ResponseEntity<RegisterResponse> me(Authentication authentication) {
        // JwtAuthenticationFilter set the principal, so getName() is the email
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));


        return ResponseEntity.ok(new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        ));
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String userAccess(){
        return "You have user level access";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess(){
        return "You have admin level access";
    }


    }