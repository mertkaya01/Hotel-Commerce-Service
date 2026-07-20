package com.stajproje.hotel.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String hotelCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String countryCode;

    @Column(nullable = false)
    private String countryName;

    private String cityCode;

    @Column(nullable = false)
    private String cityName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HotelRating rating;

    private String address;

    @Lob
    private String description;

    @Lob
    private String facilities;

    private Double latitude;

    private Double longitude;

    private String phoneNumber;

    private String websiteUrl;

    // Otelin en ucuz oda gecelik fiyati. Arama kartinda gosterilir ve fiyat
    // filtresinde kullanilir. Import/otel-ekleme aninda odalardan hesaplanip
    // saklanir (index aninda tekrar oda sorgusu yapmamak icin). CSV importlarinda
    // ve ev sahibi otellerinde dolar; teorik olarak odasiz otelde null olabilir.
    private java.math.BigDecimal minPrice;

    // Yayın durumu: CSV importları ve onaylananlar APPROVED (aramada görünür),
    // ev sahibinin eklediği yeni oteller onaya kadar PENDING.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Builder.Default
    private HotelStatus status = HotelStatus.APPROVED;

    // Oteli ekleyen ev sahibi (CSV importlarında null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    // Ev sahibinin eklediği fotoğraf URL'leri (importlarda boş)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "hotel_photos", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "photo_url", length = 1000)
    @Builder.Default
    private List<String> photos = new ArrayList<>();
}
