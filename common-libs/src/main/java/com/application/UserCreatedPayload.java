package com.application;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedPayload {
    private String name;
    private String email;
    private Long userId;

}
