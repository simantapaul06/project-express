package com.projectexpress.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationResponse {

    private Long id;

    private String country;

    private String state;

    private String city;

    private String address1;

    private String address2;

    private String pinCode;
}