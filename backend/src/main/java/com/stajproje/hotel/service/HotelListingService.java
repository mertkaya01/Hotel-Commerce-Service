package com.stajproje.hotel.service;

import com.stajproje.hotel.dto.host.HotelListingRequest;
import com.stajproje.hotel.dto.host.HotelListingResponse;
import com.stajproje.hotel.dto.host.RoomInput;
import com.stajproje.hotel.entity.Hotel;
import com.stajproje.hotel.entity.HotelRating;
import com.stajproje.hotel.entity.HotelStatus;
import com.stajproje.hotel.entity.Room;
import com.stajproje.hotel.entity.RoomType;
import com.stajproje.hotel.entity.User;
import com.stajproje.hotel.exception.HostApplicationException;
import com.stajproje.hotel.exception.HotelNotFoundException;
import com.stajproje.hotel.repository.HotelRepository;
import com.stajproje.hotel.repository.RoomRepository;
import com.stajproje.hotel.repository.UserRepository;
import com.stajproje.hotel.solr.SolrHotelIndexer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelListingService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final SolrHotelIndexer solrIndexer;
    private final AuditService auditService;

    /** Ev sahibi yeni otel ekler -> PENDING (aramada henüz YOK, Solr'a yazılmaz). */
    @Transactional
    public HotelListingResponse submit(Long userId, HotelListingRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanici bulunamadi"));

        Hotel hotel = Hotel.builder()
                .hotelCode("HOST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .name(request.getName())
                .countryCode("")
                .countryName(request.getCountryName())
                .cityName(request.getCityName())
                .rating(parseRating(request.getRating()))
                .address(request.getAddress())
                .description(request.getDescription())
                // olanak anahtarlarını facilities metnine çevir (parseAmenities bunu okur)
                .facilities(request.getAmenities() == null ? null : String.join(" ", request.getAmenities()))
                .photos(new ArrayList<>(request.getPhotos()))
                // en ucuz oda fiyati — arama kartinda + fiyat filtresinde kullanilir
                .minPrice(minRoomPrice(request.getRooms()))
                .owner(owner)
                .status(HotelStatus.PENDING)
                .build();
        hotelRepository.save(hotel);

        for (RoomInput r : request.getRooms()) {
            roomRepository.save(Room.builder()
                    .hotel(hotel)
                    .roomNumber(String.valueOf(100 + request.getRooms().indexOf(r) + 1))
                    .roomType(parseRoomType(r.getRoomType()))
                    .capacity(r.getCapacity())
                    .pricePerNight(r.getPricePerNight())
                    .build());
        }

        auditService.log(com.stajproje.hotel.entity.AuditEventType.HOTEL_SUBMITTED,
                "Otel eklendi (onay bekliyor): " + hotel.getName());
        return toResponse(hotel, request.getRooms().size());
    }

    /** Ev sahibinin eklediği oteller + durumları. */
    public List<HotelListingResponse> getMyListings(Long userId) {
        return hotelRepository.findByOwnerIdOrderByIdDesc(userId).stream()
                .map(h -> toResponse(h, roomRepository.findByHotelId(h.getId()).size()))
                .toList();
    }

    /** SUPER_ADMIN: onay bekleyen otel eklemeleri. */
    public List<HotelListingResponse> listPending() {
        return hotelRepository.findByStatusOrderByIdDesc(HotelStatus.PENDING).stream()
                .map(h -> toResponse(h, roomRepository.findByHotelId(h.getId()).size()))
                .toList();
    }

    /** SUPER_ADMIN: oteli onaylar -> APPROVED + Solr'a index (aramada görünür). */
    @Transactional
    public HotelListingResponse approve(Long hotelId) throws Exception {
        Hotel hotel = getPendingOrThrow(hotelId);
        hotel.setStatus(HotelStatus.APPROVED);
        hotelRepository.save(hotel);

        // artık aramada görünsün
        solrIndexer.index(hotel);
        solrIndexer.commit();

        log.info("Otel onaylandi ve indexlendi: {} ({})", hotel.getName(), hotel.getHotelCode());
        auditService.log(com.stajproje.hotel.entity.AuditEventType.HOTEL_APPROVED,
                "Otel onaylandı: " + hotel.getName());
        return toResponse(hotel, roomRepository.findByHotelId(hotel.getId()).size());
    }

    /** SUPER_ADMIN: oteli reddeder (Solr'a yazılmaz). */
    @Transactional
    public HotelListingResponse reject(Long hotelId) {
        Hotel hotel = getPendingOrThrow(hotelId);
        hotel.setStatus(HotelStatus.REJECTED);
        hotelRepository.save(hotel);
        auditService.log(com.stajproje.hotel.entity.AuditEventType.HOTEL_REJECTED,
                "Otel reddedildi: " + hotel.getName());
        return toResponse(hotel, roomRepository.findByHotelId(hotel.getId()).size());
    }

    private Hotel getPendingOrThrow(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(String.valueOf(hotelId)));
        if (hotel.getStatus() != HotelStatus.PENDING) {
            throw new HostApplicationException("Bu otel zaten değerlendirilmiş");
        }
        return hotel;
    }

    private HotelListingResponse toResponse(Hotel h, int roomCount) {
        return HotelListingResponse.builder()
                .id(h.getId())
                .hotelCode(h.getHotelCode())
                .name(h.getName())
                .cityName(h.getCityName())
                .countryName(h.getCountryName())
                .rating(h.getRating() != null ? h.getRating().name() : null)
                .status(h.getStatus().name())
                .ownerName(h.getOwner() != null ? h.getOwner().getFirstName() + " " + h.getOwner().getLastName() : null)
                .photos(h.getPhotos())
                .roomCount(roomCount)
                .build();
    }

    /** Istenen odalarin en dusuk gecelik fiyati (rooms @NotEmpty oldugu icin bos gelmez). */
    private java.math.BigDecimal minRoomPrice(List<RoomInput> rooms) {
        return rooms.stream()
                .map(RoomInput::getPricePerNight)
                .filter(java.util.Objects::nonNull)
                .min(java.math.BigDecimal::compareTo)
                .orElse(null);
    }

    private HotelRating parseRating(String raw) {
        if (raw == null) return HotelRating.UNRATED;
        try {
            return HotelRating.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return HotelRating.UNRATED;
        }
    }

    private RoomType parseRoomType(String raw) {
        try {
            return RoomType.valueOf(raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            return RoomType.DOUBLE;
        }
    }
}
