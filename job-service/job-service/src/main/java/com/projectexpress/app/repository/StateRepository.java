package com.projectexpress.app.repository;

import com.projectexpress.app.entity.Country;
import com.projectexpress.app.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StateRepository extends JpaRepository<State, Long> {
    Optional<State> findByNameAndCountry(String state, Country country);
}