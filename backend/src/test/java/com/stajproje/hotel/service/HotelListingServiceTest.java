package com.stajproje.hotel.service;

import com.stajproje.hotel.dto.host.HotelListingRequest;
import com.stajproje.hotel.dto.host.HotelListingResponse;
import com.stajproje.hotel.dto.host.RoomInput;
import com.stajproje.hotel.entity.Hotel;
import com.stajproje.hotel.entity.HotelRating;
import com.stajproje.hotel.entity.HotelStatus;
import com.stajproje.hotel.entity.User;
import com.stajproje.hotel.exception.HostApplicationException;
import com.stajproje.hotel.exception.HotelNotFoundException;
import com.stajproje.hotel.repository.HotelRepository;
import com.stajproje.hotel.repository.RoomRepository;
import com.stajproje.hotel.repository.UserRepository;
import com.stajproje.hotel.solr.SolrHotelIndexer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Otel ekleme/onay akisi. En kritik kural: onaylanmayan otel Solr'a YAZILMAMALI
 * (yani aramada gorunmemeli). Onay aninda indexlenmeli.
 */
@ExtendWith(MockitoExtension.class)
class HotelListingServiceTest {

    @Mock private HotelRepository hotelRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private UserRepository userRepository;
    @Mock private SolrHotelIndexer solrIndexer;

    @Mock private AuditService auditService;

    @InjectMocks private HotelListingService listingService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(7L).firstName("Ev").lastName("Sahibi").email("host@x.com").build();
    }

    private HotelListingRequest request() {
        RoomInput room = new RoomInput();
        room.setRoomType("DOUBLE");
        room.setCapacity(2);
        room.setPricePerNight(new BigDecimal("500"));

        HotelListingRequest req = new HotelListingRequest();
        req.setName("Test Butik Otel");
        req.setCountryName("Turkey");
        req.setCityName("Bodrum");
        req.setRating("FOUR_STAR");
        req.setAddress("Sahil yolu 1");
        req.setDescription("Denize sifir konumda sik bir butik otel deneyimi sunar.");
        req.setAmenities(List.of("wifi", "pool"));
        req.setPhotos(new ArrayList<>(List.of("/uploads/a.jpg", "/uploads/b.jpg")));
        req.setRooms(List.of(room));
        return req;
    }

    private Hotel pendingHotel() {
        return Hotel.builder()
                .id(50L)
                .hotelCode("HOST-ABC12345")
                .name("Test Butik Otel")
                .cityName("Bodrum")
                .countryName("Turkey")
                .rating(HotelRating.FOUR_STAR)
                .photos(new ArrayList<>())
                .owner(owner)
                .status(HotelStatus.PENDING)
                .build();
    }

    @Test
    void submit_shouldCreatePendingHotel_andNotIndexToSolr() throws Exception {
        when(userRepository.findById(7L)).thenReturn(Optional.of(owner));
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(inv -> inv.getArgument(0));

        HotelListingResponse response = listingService.submit(7L, request());

        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getRoomCount()).isEqualTo(1);
        assertThat(response.getOwnerName()).isEqualTo("Ev Sahibi");
        assertThat(response.getHotelCode()).startsWith("HOST-");

        // EN ONEMLI KURAL: onaylanmadan aramaya girmemeli
        verify(solrIndexer, never()).index(any());
        verify(solrIndexer, never()).commit();
    }

    @Test
    void submit_shouldSetMinPrice_toCheapestRoom() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(owner));
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(inv -> inv.getArgument(0));

        // request() tek oda (500) donuyor; ikinci daha ucuz oda ekleyelim
        HotelListingRequest req = request();
        RoomInput cheap = new RoomInput();
        cheap.setRoomType("SINGLE");
        cheap.setCapacity(1);
        cheap.setPricePerNight(new BigDecimal("250"));
        req.setRooms(List.of(req.getRooms().get(0), cheap));

        listingService.submit(7L, req);

        // en ucuz oda (250) otelin minPrice'i olmali
        verify(hotelRepository).save(org.mockito.ArgumentMatchers.argThat(
                h -> h.getMinPrice() != null && h.getMinPrice().compareTo(new BigDecimal("250")) == 0));
    }

    @Test
    void submit_shouldMapAmenitiesToFacilitiesText() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(owner));
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(inv -> inv.getArgument(0));

        listingService.submit(7L, request());

        // olanak anahtarlari facilities metnine yazilir (arama/filtre bunu okur)
        verify(hotelRepository).save(org.mockito.ArgumentMatchers.argThat(
                h -> "wifi pool".equals(h.getFacilities())
                        && h.getStatus() == HotelStatus.PENDING
                        && h.getPhotos().size() == 2));
    }

    @Test
    void submit_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.submit(99L, request()))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void submit_shouldFallbackToUnrated_whenRatingInvalid() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(owner));
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(inv -> inv.getArgument(0));

        HotelListingRequest req = request();
        req.setRating("ALTIN_YILDIZ"); // gecersiz

        HotelListingResponse response = listingService.submit(7L, req);

        assertThat(response.getRating()).isEqualTo("UNRATED");
    }

    @Test
    void approve_shouldSetApproved_andIndexToSolr() throws Exception {
        Hotel hotel = pendingHotel();
        when(hotelRepository.findById(50L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(inv -> inv.getArgument(0));
        when(roomRepository.findByHotelId(50L)).thenReturn(List.of());

        HotelListingResponse response = listingService.approve(50L);

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        assertThat(hotel.getStatus()).isEqualTo(HotelStatus.APPROVED);

        // onay aninda aramaya girmeli
        verify(solrIndexer).index(hotel);
        verify(solrIndexer).commit();
    }

    @Test
    void reject_shouldSetRejected_andNeverIndex() throws Exception {
        Hotel hotel = pendingHotel();
        when(hotelRepository.findById(50L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(inv -> inv.getArgument(0));
        when(roomRepository.findByHotelId(50L)).thenReturn(List.of());

        HotelListingResponse response = listingService.reject(50L);

        assertThat(response.getStatus()).isEqualTo("REJECTED");
        verify(solrIndexer, never()).index(any());
    }

    @Test
    void approve_shouldThrow_whenHotelAlreadyReviewed() throws Exception {
        Hotel hotel = pendingHotel();
        hotel.setStatus(HotelStatus.APPROVED); // zaten onaylanmis
        when(hotelRepository.findById(50L)).thenReturn(Optional.of(hotel));

        assertThatThrownBy(() -> listingService.approve(50L))
                .isInstanceOf(HostApplicationException.class);

        // iki kez indexlenmemeli
        verify(solrIndexer, never()).index(any());
    }

    @Test
    void approve_shouldThrow_whenHotelNotFound() {
        when(hotelRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.approve(404L))
                .isInstanceOf(HotelNotFoundException.class);
    }

    @Test
    void listPending_shouldOnlyReturnPendingHotels() {
        when(hotelRepository.findByStatusOrderByIdDesc(HotelStatus.PENDING))
                .thenReturn(List.of(pendingHotel()));
        when(roomRepository.findByHotelId(50L)).thenReturn(List.of());

        List<HotelListingResponse> pending = listingService.listPending();

        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).getStatus()).isEqualTo("PENDING");
    }
}
