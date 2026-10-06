package com.projectexpress.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationRequest {

    @NotNull(message = "Country is required")
    private Long countryId;

    @NotNull(message = "State is required")
    private Long stateId;

    @NotNull(message = "City is required")
    private Long cityId;

    @NotBlank(message = "Address 1 is required")
    private String address1;

    private String address2;

    @NotBlank(message = "PIN code is required")
    private String pinCode;
}