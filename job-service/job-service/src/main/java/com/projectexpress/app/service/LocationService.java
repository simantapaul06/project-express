package com.projectexpress.app.service;

import com.projectexpress.app.dto.LocationRequest;
import com.projectexpress.app.dto.LocationUpdateRequest;
import com.projectexpress.app.entity.City;
import com.projectexpress.app.entity.Country;
import com.projectexpress.app.entity.Location;
import com.projectexpress.app.entity.State;
import com.projectexpress.app.exception.ResourceNotFoundException;
import com.projectexpress.app.repository.CityRepository;
import com.projectexpress.app.repository.CountryRepository;
import com.projectexpress.app.repository.LocationRepository;
import com.projectexpress.app.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final LocationRepository locationRepository;

    public void validateLocation(Long countryId, Long stateId, Long cityId) {

        Country country = countryRepository.findById(countryId)
                .orElseThrow(() ->
                        new RuntimeException("Country not found"));

        State state = stateRepository.findById(stateId)
                .orElseThrow(() ->
                        new RuntimeException("State not found"));

        City city = cityRepository.findById(cityId)
                .orElseThrow(() ->
                        new RuntimeException("City not found"));

        // Check State belongs to Country
        if (!state.getCountry().getId().equals(country.getId())) {
            throw new RuntimeException(
                    "State does not belong to the selected country"
            );
        }

        // Check City belongs to State
        if (!city.getState().getId().equals(state.getId())) {
            throw new RuntimeException(
                    "City does not belong to the selected state"
            );
        }
    }


    //create location
    public Location createLocation(LocationRequest request) {

        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() ->
                        new RuntimeException("Country not found"));

        State state = stateRepository.findById(request.getStateId())
                .orElseThrow(() ->
                        new RuntimeException("State not found"));

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() ->
                        new RuntimeException("City not found"));

        // Check State belongs to Country
        if (!state.getCountry().getId().equals(country.getId())) {
            throw new RuntimeException(
                    "State does not belong to the selected country"
            );
        }

        // Check City belongs to State
        if (!city.getState().getId().equals(state.getId())) {
            throw new RuntimeException(
                    "City does not belong to the selected state"
            );
        }

        Location location = Location.builder()
                .country(country)
                .state(state)
                .city(city)
                .address1(request.getAddress1())
                .address2(request.getAddress2())
                .pinCode(request.getPinCode())
                .build();

        return locationRepository.save(location);
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Location getLocationById(Long id) {

        return locationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Location not found with id: " + id));
    }


    public Location updateLocation(Long id, LocationUpdateRequest request) {

        Location existingLocation = locationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Location not found with id: " + id));

        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Country not found"));

        State state = stateRepository.findById(request.getStateId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "State not found"));

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "City not found"));

        // validate State is present in this Country or not
        if (!state.getCountry().getId().equals(country.getId())) {
            throw new RuntimeException(
                    "State does not belong to the selected country");
        }

        // Validate City is present in this State or not
        if (!city.getState().getId().equals(state.getId())) {
            throw new RuntimeException(
                    "City does not belong to the selected state");
        }

        existingLocation.setCountry(country);
        existingLocation.setState(state);
        existingLocation.setCity(city);
        existingLocation.setAddress1(request.getAddress1());
        existingLocation.setAddress2(request.getAddress2());
        existingLocation.setPinCode(request.getPinCode());

        return locationRepository.save(existingLocation);
    }


    public void deleteLocation(Long id) {

        Location existingLocation = locationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Location not found with id: " + id));

        locationRepository.delete(existingLocation);
    }
}
