package com.projectexpress.app.repository;

import com.projectexpress.app.entity.City;
import com.projectexpress.app.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameAndState(String cityName, State state);
}