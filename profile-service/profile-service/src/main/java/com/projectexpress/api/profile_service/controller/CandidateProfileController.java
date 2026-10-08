package com.projectexpress.api.profile_service.controller;

import com.projectexpress.api.profile_service.dto.ProfileRequest;
import com.projectexpress.api.profile_service.dto.ProfileResponse;
import com.projectexpress.api.profile_service.security.AuthenticatedUser;
import com.projectexpress.api.profile_service.service.CandidateProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProfileResponse> createOrUpdateMyProfile(@Valid @RequestBody ProfileRequest request, Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        ProfileResponse profile = candidateProfileService.createOrUpdateProfile(
                                            user.getUserId(), request);

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProfileResponse> getMyProfile(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(candidateProfileService.getMyProfile(user.getUserId()));
    }


    //******************************************
    // Profile Photo
    //******************************************
    @PostMapping(
            value = "/me/photo",
            consumes = "multipart/form-data"
    )
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProfileResponse> uploadProfilePhoto(@RequestParam("file") MultipartFile file, Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        ProfileResponse response = candidateProfileService.uploadProfilePhoto(
                        user.getUserId(),
                        file
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/photo")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<byte[]> getProfilePhoto(Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        Long candidateId = user.getUserId();

        byte[] image = candidateProfileService.getProfilePhoto(candidateId);

        String contentType = candidateProfileService.getProfilePhotoContentType(candidateId);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, contentType).body(image);
    }

    @DeleteMapping("/me/photo")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteProfilePhoto(Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        candidateProfileService.deleteProfilePhoto(user.getUserId());

        return ResponseEntity.noContent().build();
    }



    //******************************************
    // Resume
    //******************************************
    @PostMapping(
            value = "/me/resume",
            consumes = "multipart/form-data"
    )
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProfileResponse> uploadResume(@RequestParam("file") MultipartFile file, Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        ProfileResponse response = candidateProfileService.uploadResume(user.getUserId(), file);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/resume")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<byte[]> getResume(Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        byte[] resume = candidateProfileService.getResume(user.getUserId());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"resume.pdf\"")
                .body(resume);
    }

    @DeleteMapping("/me/resume")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteResume(Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        candidateProfileService.deleteResume(user.getUserId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{candidateId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileResponse> getProfileByCandidateId(
            @PathVariable Long candidateId) {

        ProfileResponse response =
                candidateProfileService.getProfileByCandidateId(candidateId);

        return ResponseEntity.ok(response);
    }

}