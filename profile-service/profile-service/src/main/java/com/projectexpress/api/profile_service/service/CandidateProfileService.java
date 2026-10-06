package com.projectexpress.api.profile_service.service;

import com.projectexpress.api.profile_service.dto.ProfileRequest;
import com.projectexpress.api.profile_service.dto.ProfileResponse;
import com.projectexpress.api.profile_service.entity.CandidateProfile;
import com.projectexpress.api.profile_service.exception.ProfileNotFoundException;
import com.projectexpress.api.profile_service.repository.CandidateProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidateProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final ObjectStorageService objectStorageService;

    public ProfileResponse createOrUpdateProfile(Long candidateId, ProfileRequest request) {

        CandidateProfile profile = candidateProfileRepository
                .findByCandidateId(candidateId)
                .orElseGet(() -> CandidateProfile.builder()
                        .candidateId(candidateId)
                        .build());

        profile.setName(request.name());
        profile.setEmail(request.email());
        profile.setPhone(request.phone());

        CandidateProfile saved = candidateProfileRepository.save(profile);

        return toResponse(saved);
    }

    public ProfileResponse getMyProfile(Long candidateId) {

        CandidateProfile profile = candidateProfileRepository
                .findByCandidateId(candidateId)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));

        return toResponse(profile);
    }

    private ProfileResponse toResponse(CandidateProfile profile) {

        return new ProfileResponse(
                profile.getId(),
                profile.getCandidateId(),
                profile.getName(),
                profile.getEmail(),
                profile.getPhone(),
                profile.getProfileImageKey(),
                profile.getResumeKey()
        );
    }

    public ProfileResponse uploadProfilePhoto(Long candidateId, MultipartFile file) {

        CandidateProfile profile = candidateProfileRepository
                .findByCandidateId(candidateId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Profile photo cannot be empty");
        }

        String contentType = file.getContentType();

        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {

            throw new IllegalArgumentException("Only JPEG and PNG images are allowed");
        }

        String extension = contentType.equals("image/png") ? ".png" : ".jpg";

        // Remember the old photo
        String oldImageKey = profile.getProfileImageKey();

        // Generate a new key
        String newImageKey = "profile-images/" + candidateId + "/" + UUID.randomUUID() + extension;

        // 1. Upload new image first
        objectStorageService.upload(newImageKey, file);

        // 2. Update database
        profile.setProfileImageKey(newImageKey);
        CandidateProfile saved = candidateProfileRepository.save(profile);

        // 3. Only after successful upload + DB update,
        // delete the previous image
        if (oldImageKey != null && !oldImageKey.isBlank()) {
            objectStorageService.delete(oldImageKey);
        }

        return toResponse(saved);
    }

    public byte[] getProfilePhoto(Long candidateId) {

        CandidateProfile profile = candidateProfileRepository
                .findByCandidateId(candidateId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        String profileImageKey = profile.getProfileImageKey();

        if (profileImageKey == null || profileImageKey.isBlank()) {
            throw new RuntimeException("Profile photo not found");
        }

        return objectStorageService.download(profileImageKey);
    }

    public String getProfilePhotoContentType(Long candidateId) {

        CandidateProfile profile = candidateProfileRepository
                .findByCandidateId(candidateId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        String key = profile.getProfileImageKey();

        if (key == null || key.isBlank()) {
            throw new RuntimeException("Profile photo not found");
        }

        if (key.toLowerCase().endsWith(".png")) {
            return "image/png";
        }

        return "image/jpeg";
    }

    public void deleteProfilePhoto(Long candidateId) {

        CandidateProfile profile = candidateProfileRepository
                .findByCandidateId(candidateId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        String imageKey = profile.getProfileImageKey();

        if (imageKey == null || imageKey.isBlank()) {
            throw new RuntimeException("Profile photo not found");
        }

        // Delete actual image from SeaweedFS
        objectStorageService.delete(imageKey);

        // Remove reference from database
        profile.setProfileImageKey(null);

        candidateProfileRepository.save(profile);
    }

    public ProfileResponse uploadResume(Long candidateId, MultipartFile file) {

        CandidateProfile profile = candidateProfileRepository
                .findByCandidateId(candidateId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume cannot be empty");
        }

        // Only PDF is allowed
        String contentType = file.getContentType();

        if (contentType == null || !contentType.equals("application/pdf")) {

            throw new IllegalArgumentException("Only PDF resume is allowed");
        }

        // Keep old resume key because this may be a replacement
        String oldResumeKey = profile.getResumeKey();

        String newResumeKey = "resumes/" + candidateId + "/" + UUID.randomUUID() + ".pdf";

        // 1. Upload new PDF
        objectStorageService.upload(newResumeKey, file);

        // 2. Store new key in MySQL
        profile.setResumeKey(newResumeKey);

        CandidateProfile saved = candidateProfileRepository.save(profile);

        // 3. Delete previous resume after successful replacement
        if (oldResumeKey != null && !oldResumeKey.isBlank()) {
            objectStorageService.delete(oldResumeKey);
        }

        return toResponse(saved);
    }

    public byte[] getResume(Long candidateId) {

        CandidateProfile profile = candidateProfileRepository
                .findByCandidateId(candidateId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        String resumeKey = profile.getResumeKey();

        if (resumeKey == null || resumeKey.isBlank()) {
            throw new RuntimeException("Resume not found");
        }

        return objectStorageService.download(resumeKey);
    }

    public void deleteResume(Long candidateId) {

        CandidateProfile profile = candidateProfileRepository
                .findByCandidateId(candidateId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        String resumeKey = profile.getResumeKey();

        if (resumeKey == null || resumeKey.isBlank()) {
            throw new RuntimeException("Resume not found");
        }

        // Delete actual PDF from SeaweedFS
        objectStorageService.delete(resumeKey);

        // Remove the reference from MySQL
        profile.setResumeKey(null);

        candidateProfileRepository.save(profile);
    }


    public ProfileResponse getProfileByCandidateId(Long candidateId) {

        CandidateProfile profile = candidateProfileRepository.findByCandidateId(candidateId)
                        .orElseThrow(() -> new ProfileNotFoundException(
                                "Profile not found for candidateId: " + candidateId
                        ));

        return toResponse(profile);
    }
}