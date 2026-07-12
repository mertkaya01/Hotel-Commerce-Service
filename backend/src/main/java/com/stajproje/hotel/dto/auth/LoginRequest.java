package com.stajproje.hotel.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Email bos olamaz")
    @Email(message = "Gecerli bir email giriniz")
    private String email;

    @NotBlank(message = "Sifre bos olamaz")
    private String password;
}
