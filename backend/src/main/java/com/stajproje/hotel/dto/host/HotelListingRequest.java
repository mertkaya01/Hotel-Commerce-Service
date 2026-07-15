package com.stajproje.hotel.dto.host;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HotelListingRequest {

    @NotBlank(message = "Otel adi bos olamaz")
    private String name;

    @NotBlank(message = "Ulke bos olamaz")
    private String countryName;

    @NotBlank(message = "Sehir bos olamaz")
    private String cityName;

    // ONE_STAR .. FIVE_STAR / UNRATED
    private String rating;

    private String address;

    @NotBlank(message = "Aciklama bos olamaz")
    @Size(min = 20, message = "Aciklama en az 20 karakter olmali")
    private String description;

    // Ev sahibinin girdigi olanak anahtarlari (wifi, pool, ... — facilities'e cevrilir)
    private List<String> amenities;

    // Fotograf URL'leri
    @NotEmpty(message = "En az bir fotograf ekleyin")
    private List<@NotBlank String> photos;

    @NotEmpty(message = "En az bir oda ekleyin")
    @Valid
    private List<RoomInput> rooms;
}
