package com.projectexpress.app.repository;

import com.projectexpress.app.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
}
