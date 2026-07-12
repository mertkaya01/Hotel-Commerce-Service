package com.stajproje.hotel.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank(message = "Ad bos olamaz")
    private String firstName;

    @NotBlank(message = "Soyad bos olamaz")
    private String lastName;
}
