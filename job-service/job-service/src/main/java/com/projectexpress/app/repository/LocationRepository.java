package com.projectexpress.app.repository;

import com.projectexpress.app.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}