package com.stajproje.hotel.batch;

import com.stajproje.hotel.entity.Hotel;
import com.stajproje.hotel.entity.HotelRating;
import com.stajproje.hotel.entity.Room;
import com.stajproje.hotel.entity.RoomType;
import com.stajproje.hotel.repository.HotelRepository;
import com.stajproje.hotel.repository.RoomRepository;
import com.stajproje.hotel.solr.SolrHotelIndexer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Slf4j
@Component
@Order(1)
@Profile("!test")
@RequiredArgsConstructor
public class CsvImportRunner implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final SolrHotelIndexer solrIndexer;

    @Value("${app.import.csv-path}")
    private String csvPath;

    private static final Random RANDOM = new Random();

    @Override
    public void run(String... args) throws Exception {
        if (hotelRepository.count() > 0) {
            log.info("Hotel verisi zaten yuklu, CSV import atlaniyor.");
            return;
        }

        log.info("CSV import basliyor: {}", csvPath);
        int imported = 0;
        int duplicateSkipped = 0;
        Set<String> seenHotelCodes = new HashSet<>();

        try (var inputStream = new ClassPathResource(csvPath).getInputStream();
             var reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreSurroundingSpaces(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                Hotel hotel = toHotel(record);
                if (hotel == null) {
                    continue;
                }

                if (!seenHotelCodes.add(hotel.getHotelCode())) {
                    duplicateSkipped++;
                    continue;
                }

                hotelRepository.save(hotel);
                List<Room> rooms = generateRooms(hotel);
                roomRepository.saveAll(rooms);
                solrIndexer.index(hotel);
                imported++;

                if (imported % 500 == 0) {
                    log.info("{} otel import edildi...", imported);
                }
            }
        }

        solrIndexer.commit();
        log.info("CSV import tamamlandi. Toplam {} otel yuklendi, {} duplicate hotelCode atlandi.", imported, duplicateSkipped);
    }

    private Hotel toHotel(CSVRecord record) {
        String hotelCode = get(record, "HotelCode");
        String name = get(record, "HotelName");
        if (hotelCode.isBlank() || name.isBlank()) {
            return null;
        }

        double[] latLon = parseLatLon(get(record, "Map"));

        return Hotel.builder()
                .hotelCode(hotelCode)
                .name(name)
                .countryCode(get(record, "countyCode"))
                .countryName(get(record, "countyName"))
                .cityCode(get(record, "cityCode"))
                .cityName(get(record, "cityName"))
                .rating(parseRating(get(record, "HotelRating")))
                .address(blankToNull(get(record, "Address")))
                .description(blankToNull(get(record, "Description")))
                .facilities(blankToNull(get(record, "HotelFacilities")))
                .latitude(latLon != null ? latLon[0] : null)
                .longitude(latLon != null ? latLon[1] : null)
                .phoneNumber(blankToNull(get(record, "PhoneNumber")))
                .websiteUrl(blankToNull(get(record, "HotelWebsiteUrl")))
                .build();
    }

    private List<Room> generateRooms(Hotel hotel) {
        int count = 2 + RANDOM.nextInt(3);
        List<Room> rooms = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            RoomType type = RoomType.values()[RANDOM.nextInt(RoomType.values().length)];
            rooms.add(Room.builder()
                    .hotel(hotel)
                    .roomNumber(String.valueOf(100 + i))
                    .roomType(type)
                    .capacity(capacityFor(type))
                    .pricePerNight(priceFor(type))
                    .build());
        }
        return rooms;
    }

    private int capacityFor(RoomType type) {
        return switch (type) {
            case SINGLE -> 1;
            case DOUBLE -> 2;
            case DELUXE -> 3;
            case SUITE -> 4;
        };
    }

    private BigDecimal priceFor(RoomType type) {
        double min = switch (type) {
            case SINGLE -> 40;
            case DOUBLE -> 70;
            case DELUXE -> 120;
            case SUITE -> 200;
        };
        double max = switch (type) {
            case SINGLE -> 90;
            case DOUBLE -> 150;
            case DELUXE -> 250;
            case SUITE -> 500;
        };
        double price = min + RANDOM.nextDouble() * (max - min);
        return BigDecimal.valueOf(Math.round(price * 100.0) / 100.0);
    }

    private HotelRating parseRating(String raw) {
        return switch (raw.trim()) {
            case "OneStar" -> HotelRating.ONE_STAR;
            case "TwoStar" -> HotelRating.TWO_STAR;
            case "ThreeStar" -> HotelRating.THREE_STAR;
            case "FourStar" -> HotelRating.FOUR_STAR;
            case "FiveStar" -> HotelRating.FIVE_STAR;
            default -> HotelRating.UNRATED;
        };
    }

    private double[] parseLatLon(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String[] parts = raw.split("\\|");
        if (parts.length != 2) {
            return null;
        }
        try {
            double lat = Double.parseDouble(parts[0].trim());
            double lon = Double.parseDouble(parts[1].trim());
            return new double[]{lat, lon};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String get(CSVRecord record, String column) {
        return record.isSet(column) ? record.get(column).trim() : "";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
