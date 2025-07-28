package com.finwise.authservice.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    private String emailOrUserId;
    private String password;
}
