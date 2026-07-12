package com.stajproje.hotel.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Email bos olamaz")
    @Email(message = "Gecerli bir email giriniz")
    private String email;

    @NotBlank(message = "Sifre bos olamaz")
    @Size(min = 8, message = "Sifre en az 8 karakter olmali")
    private String password;

    @NotBlank(message = "Ad bos olamaz")
    private String firstName;

    @NotBlank(message = "Soyad bos olamaz")
    private String lastName;
}
