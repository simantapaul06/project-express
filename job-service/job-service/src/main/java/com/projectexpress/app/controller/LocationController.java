package com.projectexpress.app.controller;

import com.projectexpress.app.dto.JobResponse;
import com.projectexpress.app.dto.LocationRequest;
import com.projectexpress.app.dto.LocationResponse;
import com.projectexpress.app.dto.LocationUpdateRequest;
import com.projectexpress.app.entity.Job;
import com.projectexpress.app.entity.Location;
import com.projectexpress.app.mapper.LocationMapper;
import com.projectexpress.app.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final LocationMapper locationMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(
            @Valid @RequestBody LocationRequest request) {

        Location location = locationService.createLocation(request);

        LocationResponse response = new LocationResponse(
                location.getId(),
                location.getCountry().getName(),
                location.getState().getName(),
                location.getCity().getName(),
                location.getAddress1(),
                location.getAddress2(),
                location.getPinCode()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations() {
        List<LocationResponse> locations = locationService.getAllLocations().stream()
                                                                        .map(locationMapper::toResponse)
                                                                        .toList();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable Long id) {

        Location location = locationService.getLocationById(id);

        return ResponseEntity.ok(locationMapper.toResponse(location));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> updateLocation(@PathVariable Long id, @Valid @RequestBody LocationUpdateRequest request) {

        Location location = locationService.updateLocation(id, request);
        return ResponseEntity.ok(locationMapper.toResponse(location));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {

        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}
