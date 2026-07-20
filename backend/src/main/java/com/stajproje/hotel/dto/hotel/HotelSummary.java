package com.stajproje.hotel.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class HotelSummary {

    private String hotelCode;
    private String name;
    private String countryName;
    private String cityName;
    private String rating;
    // en ucuz oda gecelik fiyati (arama kartinda "X TL'den baslayan")
    private BigDecimal minPrice;
}
