package com.stajproje.hotel.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelDetailResponse {

    private Long id;
    private String hotelCode;
    private String name;
    private String countryName;
    private String cityName;
    private String rating;
    private String address;
    private String description;
    private String facilities;
    private String phoneNumber;
    private String websiteUrl;
    private Double latitude;
    private Double longitude;
    // ev sahibinin eklediği fotoğraflar (boşsa frontend üretilen görselleri kullanır)
    private List<String> photos;
}
