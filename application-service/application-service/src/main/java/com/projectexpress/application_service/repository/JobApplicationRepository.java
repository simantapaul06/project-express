package com.projectexpress.application_service.repository;

import com.projectexpress.application_service.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);
    List<JobApplication> findByCandidateId(Long candidateId);
    List<JobApplication> findByJobId(Long jobId);
    Optional<JobApplication> findByIdAndCandidateId(Long id, Long candidateId);
    Optional<JobApplication> findByCandidateIdAndJobId(Long candidateId, Long jobId);
}
