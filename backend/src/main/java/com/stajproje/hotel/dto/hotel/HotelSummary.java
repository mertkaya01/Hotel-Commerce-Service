package com.stajproje.hotel.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HotelSummary {

    private String hotelCode;
    private String name;
    private String countryName;
    private String cityName;
    private String rating;
}
