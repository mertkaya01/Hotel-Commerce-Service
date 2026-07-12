package com.stajproje.hotel.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class HotelSearchResponse {

    private List<HotelSummary> hotels;
    private long totalResults;
    private int page;
    private int size;
    private Map<String, List<FacetValue>> facets;
}
