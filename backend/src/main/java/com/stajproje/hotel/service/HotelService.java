package com.stajproje.hotel.service;

import com.stajproje.hotel.dto.hotel.FacetValue;
import com.stajproje.hotel.dto.hotel.HotelDetailResponse;
import com.stajproje.hotel.dto.hotel.HotelSearchResponse;
import com.stajproje.hotel.dto.hotel.HotelSummary;
import com.stajproje.hotel.dto.hotel.RoomResponse;
import com.stajproje.hotel.entity.Hotel;
import com.stajproje.hotel.exception.HotelNotFoundException;
import com.stajproje.hotel.repository.HotelRepository;
import com.stajproje.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HotelService {

    private static final String[] FACET_FIELDS = {"countryName", "cityName", "rating"};

    private final HttpJdkSolrClient solrClient;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    /**
     * Desteklenen siralamalar. Solr'da fiyat alani YOK (fiyatlar demo olarak arayuzde
     * uretiliyor), bu yuzden fiyata gore siralama sunulmuyor.
     */
    private static void applySort(SolrQuery query, String sort) {
        if (sort == null || sort.isBlank() || "relevance".equals(sort)) {
            return; // varsayilan: Solr'un alaka puani (score desc)
        }
        switch (sort) {
            case "name_asc" -> query.addSort("nameSort", SolrQuery.ORDER.asc);
            case "name_desc" -> query.addSort("nameSort", SolrQuery.ORDER.desc);
            case "rating_desc" -> query.addSort("ratingValue", SolrQuery.ORDER.desc);
            case "rating_asc" -> query.addSort("ratingValue", SolrQuery.ORDER.asc);
            default -> throw new IllegalArgumentException("Gecersiz siralama: " + sort);
        }
        // Esit degerlerde sayfalar arasi tutarli sira icin ikincil anahtar
        query.addSort("hotelCode", SolrQuery.ORDER.asc);
    }

    public HotelSearchResponse search(String q, String country, String city, String rating,
                                       String sort, int page, int size) {
        SolrQuery query = new SolrQuery();
        query.set("defType", "edismax");
        // Arama TEXT alanlarina yapilir (buyuk/kucuk harf duyarsiz). cityName/countryName
        // string alanlari yalnizca facet + filtre (fq) icin kullanilir, aramada degil.
        query.set("qf", "name^3 cityText^2 countryText^2 description facilities");
        query.set("q.alt", "*:*");
        query.setQuery(q != null && !q.isBlank() ? q : null);

        if (country != null && !country.isBlank()) {
            query.addFilterQuery("countryName:\"" + country + "\"");
        }
        if (city != null && !city.isBlank()) {
            query.addFilterQuery("cityName:\"" + city + "\"");
        }
        if (rating != null && !rating.isBlank()) {
            query.addFilterQuery("rating:\"" + rating + "\"");
        }

        applySort(query, sort);

        query.setFacet(true);
        query.addFacetField(FACET_FIELDS);
        query.setFacetLimit(20);
        query.setFacetMinCount(1);

        query.setStart(page * size);
        query.setRows(size);

        try {
            QueryResponse response = solrClient.query(query);

            List<HotelSummary> hotels = new ArrayList<>();
            for (SolrDocument doc : response.getResults()) {
                hotels.add(HotelSummary.builder()
                        .hotelCode((String) doc.getFieldValue("hotelCode"))
                        .name((String) doc.getFieldValue("name"))
                        .countryName((String) doc.getFieldValue("countryName"))
                        .cityName((String) doc.getFieldValue("cityName"))
                        .rating((String) doc.getFieldValue("rating"))
                        .build());
            }

            Map<String, List<FacetValue>> facets = new LinkedHashMap<>();
            if (response.getFacetFields() != null) {
                for (FacetField facetField : response.getFacetFields()) {
                    List<FacetValue> values = new ArrayList<>();
                    for (FacetField.Count count : facetField.getValues()) {
                        values.add(new FacetValue(count.getName(), count.getCount()));
                    }
                    facets.put(facetField.getName(), values);
                }
            }

            return HotelSearchResponse.builder()
                    .hotels(hotels)
                    .totalResults(response.getResults().getNumFound())
                    .page(page)
                    .size(size)
                    .facets(facets)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Solr arama sorgusu basarisiz oldu", e);
        }
    }

    public HotelDetailResponse getByHotelCode(String hotelCode) {
        Hotel hotel = hotelRepository.findByHotelCode(hotelCode)
                .orElseThrow(() -> new HotelNotFoundException(hotelCode));

        HotelDetailResponse response = modelMapper.map(hotel, HotelDetailResponse.class);
        response.setRating(hotel.getRating() != null ? hotel.getRating().name() : null);
        // ev sahibi fotoğrafları (open-in-view sayesinde lazy koleksiyon burada yüklenir)
        response.setPhotos(new ArrayList<>(hotel.getPhotos()));
        return response;
    }

    public List<RoomResponse> getRooms(String hotelCode) {
        Hotel hotel = hotelRepository.findByHotelCode(hotelCode)
                .orElseThrow(() -> new HotelNotFoundException(hotelCode));

        return roomRepository.findByHotelId(hotel.getId()).stream()
                .map(room -> RoomResponse.builder()
                        .id(room.getId())
                        .roomNumber(room.getRoomNumber())
                        .roomType(room.getRoomType().name())
                        .capacity(room.getCapacity())
                        .pricePerNight(room.getPricePerNight())
                        .build())
                .toList();
    }
}
