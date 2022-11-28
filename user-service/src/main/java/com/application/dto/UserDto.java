package com.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class UserDto {
    private String name;
    private String email;
    private String phone;
    private String kycId;
    private String address;
}
