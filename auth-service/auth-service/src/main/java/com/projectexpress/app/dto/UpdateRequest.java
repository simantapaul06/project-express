package com.projectexpress.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRequest {

    @NotNull(message = "id is required")
    private Long id;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "role is required")
    private String role;
}
