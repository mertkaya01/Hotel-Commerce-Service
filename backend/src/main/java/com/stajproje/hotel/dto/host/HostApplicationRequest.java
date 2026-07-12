package com.stajproje.hotel.dto.host;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class HostApplicationRequest {

    @NotBlank(message = "Ad bos olamaz")
    private String firstName;

    @NotBlank(message = "Soyad bos olamaz")
    private String lastName;

    @NotNull(message = "Dogum tarihi bos olamaz")
    @Past(message = "Dogum tarihi gecmiste olmali")
    private LocalDate birthDate;

    @NotBlank(message = "Aciklama bos olamaz")
    private String description;
}
