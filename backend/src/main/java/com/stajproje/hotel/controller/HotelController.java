package com.stajproje.hotel.controller;

import com.stajproje.hotel.dto.hotel.HotelDetailResponse;
import com.stajproje.hotel.dto.hotel.HotelSearchResponse;
import com.stajproje.hotel.dto.hotel.RoomResponse;
import com.stajproje.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping("/search")
    public HotelSearchResponse search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String rating,
            // relevance (varsayilan) | name_asc | name_desc | rating_desc | rating_asc
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return hotelService.search(q, country, city, rating, sort, page, size);
    }

    @GetMapping("/{hotelCode}")
    public HotelDetailResponse getByHotelCode(@PathVariable String hotelCode) {
        return hotelService.getByHotelCode(hotelCode);
    }

    @GetMapping("/{hotelCode}/rooms")
    public List<RoomResponse> getRooms(@PathVariable String hotelCode) {
        return hotelService.getRooms(hotelCode);
    }
}
