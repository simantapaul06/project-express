package com.projectexpress.app.mapper;

import com.projectexpress.app.dto.LocationResponse;
import com.projectexpress.app.entity.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

        public LocationResponse toResponse(Location location) {
            if (location == null) {
                return null;
            }

            return new LocationResponse(
                    location.getId(),
                    location.getCountry().getName(),
                    location.getState().getName(),
                    location.getCity().getName(),
                    location.getAddress1(),
                    location.getAddress2(),
                    location.getPinCode()
            );
        }

}
