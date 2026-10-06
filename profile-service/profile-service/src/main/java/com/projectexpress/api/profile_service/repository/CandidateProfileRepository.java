package com.projectexpress.api.profile_service.repository;

import com.projectexpress.api.profile_service.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {

    Optional<CandidateProfile> findByCandidateId(Long candidateId);

    boolean existsByCandidateId(Long candidateId);
}