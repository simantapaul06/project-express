package com.projectexpress.api.profile_service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    private Long userId;
    private String name;
    private String email;
}