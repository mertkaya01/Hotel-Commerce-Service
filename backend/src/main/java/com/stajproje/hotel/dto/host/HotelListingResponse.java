package com.stajproje.hotel.dto.host;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class HotelListingResponse {

    private Long id;
    private String hotelCode;
    private String name;
    private String cityName;
    private String countryName;
    private String rating;
    private String status;      // PENDING / APPROVED / REJECTED
    private String ownerName;   // admin listesinde başvuran adı
    private List<String> photos;
    private int roomCount;
}
