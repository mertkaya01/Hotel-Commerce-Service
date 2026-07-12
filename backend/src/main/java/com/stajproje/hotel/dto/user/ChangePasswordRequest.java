package com.stajproje.hotel.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "Mevcut sifre bos olamaz")
    private String currentPassword;

    @NotBlank(message = "Yeni sifre bos olamaz")
    @Size(min = 8, message = "Yeni sifre en az 8 karakter olmali")
    private String newPassword;
}
