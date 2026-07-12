import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Router, RouterLink } from '@angular/router';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { AuthService } from '../../core/services/auth.service';
import { FavoritesService } from '../../core/services/favorites.service';
import { HotelService } from '../../core/services/hotel.service';
import { ReservationService } from '../../core/services/reservation.service';
import { SearchDatesService } from '../../core/services/search-dates.service';
import { HotelDetail as HotelDetailModel, HotelSummary, Room } from '../../core/models/hotel.model';
import {
  DemoReview,
  demoReviewCount,
  demoReviewScore,
  demoReviews,
  hotelGallery,
  reviewScoreLabel,
} from '../../core/utils/hotel-visuals';
import { AmenityGroup, parseAmenities } from '../../core/utils/amenities';
import { StarRating } from '../../shared/components/star-rating/star-rating';
import { HotelGallery } from '../../shared/components/hotel-gallery/hotel-gallery';
import { ReviewCard } from '../../shared/components/review-card/review-card';
import { HotelCard } from '../../shared/components/hotel-card/hotel-card';
import { FeatureCard } from '../../shared/components/feature-card/feature-card';
import { Faq } from '../../shared/components/faq/faq';

interface OverviewItem {
  icon: string;
  title: string;
  lines: string[];
}

@Component({
  selector: 'app-hotel-detail',
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    RouterLink,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    StarRating,
    HotelGallery,
    ReviewCard,
    HotelCard,
    FeatureCard,
    Faq,
  ],
  templateUrl: './hotel-detail.html',
  styleUrl: './hotel-detail.scss',
})
export class HotelDetail {
  private readonly hotelService = inject(HotelService);
  private readonly reservationService = inject(ReservationService);
  private readonly authService = inject(AuthService);
  private readonly favoritesService = inject(FavoritesService);
  private readonly searchDates = inject(SearchDatesService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly sanitizer = inject(DomSanitizer);

  readonly favorites = this.favoritesService;
  readonly hotelCode = input.required<string>();

  readonly hotel = signal<HotelDetailModel | null>(null);
  readonly rooms = signal<Room[]>([]);
  readonly similar = signal<HotelSummary[]>([]);
  readonly loading = signal(true);
  readonly selectedRoom = signal<Room | null>(null);
  readonly booking = signal(false);

  readonly today = new Date();

  readonly dateRange = new FormGroup({
    checkIn: new FormControl<Date | null>(null),
    checkOut: new FormControl<Date | null>(null),
  });

  private readonly checkInSignal = signal<Date | null>(null);
  private readonly checkOutSignal = signal<Date | null>(null);

  readonly nights = computed(() => {
    const inD = this.checkInSignal();
    const outD = this.checkOutSignal();
    if (!inD || !outD) return 0;
    const ms = outD.getTime() - inD.getTime();
    return Math.max(0, Math.round(ms / (1000 * 60 * 60 * 24)));
  });

  readonly totalPrice = computed(() => {
    const room = this.selectedRoom();
    return room ? room.pricePerNight * this.nights() : 0;
  });

  // ---- görsel / demo veriler (otele sabit) ----
  readonly gallery = computed(() => hotelGallery(this.hotelCode(), 5));
  readonly reviewScore = computed(() => demoReviewScore(this.hotelCode()));
  readonly reviewCount = computed(() => demoReviewCount(this.hotelCode()));
  readonly reviewLabel = computed(() => reviewScoreLabel(this.reviewScore()));
  readonly reviews = computed<DemoReview[]>(() => demoReviews(this.hotelCode(), 4));

  readonly whyUs = [
    { icon: 'payments', title: 'En İyi Fiyat', text: 'Garantili en uygun fiyat.' },
    { icon: 'event_available', title: 'Ücretsiz İptal', text: 'Esnek iptal koşulları.' },
    { icon: 'support_agent', title: '7/24 Destek', text: 'Her an yanındayız.' },
    { icon: 'verified_user', title: 'Güvenli Ödeme', text: 'Korumalı işlemler.' },
  ];

  readonly nearby = [
    { icon: 'restaurant', label: 'Restoranlar', distance: '350 m' },
    { icon: 'local_mall', label: 'AVM', distance: '1.2 km' },
    { icon: 'museum', label: 'Müze', distance: '2.4 km' },
    { icon: 'flight', label: 'Havaalanı', distance: '18 km' },
  ];

  readonly overview = computed<OverviewItem[]>(() => [
    { icon: 'place', title: 'Konum', lines: ['Şehir merkezine 10 dakika', 'Toplu taşımaya yakın'] },
    { icon: 'king_bed', title: 'Konfor', lines: ['Klimalı odalar', 'Ücretsiz Wi-Fi', 'Akıllı TV'] },
    { icon: 'restaurant', title: 'Yeme İçme', lines: ['Açık büfe kahvaltı', 'Restoran', 'Bar'] },
    { icon: 'business_center', title: 'İş Seyahati', lines: ['Toplantı salonları', 'Business Center'] },
  ]);

  readonly mapUrl = computed<SafeResourceUrl | null>(() => {
    const h = this.hotel();
    if (!h?.latitude || !h?.longitude) return null;
    const url = `https://maps.google.com/maps?q=${h.latitude},${h.longitude}&z=14&output=embed`;
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  });

  constructor() {
    this.dateRange.valueChanges.subscribe((v) => {
      this.checkInSignal.set(v.checkIn ?? null);
      this.checkOutSignal.set(v.checkOut ?? null);
    });
  }

  ngOnInit(): void {
    // ana sayfada seçilen tarih aralığı varsa booking kutusunu önceden doldur
    const inD = this.searchDates.checkInDate;
    const outD = this.searchDates.checkOutDate;
    if (inD && outD) {
      this.dateRange.setValue({ checkIn: inD, checkOut: outD });
    }

    const code = this.hotelCode();
    this.hotelService.getByHotelCode(code).subscribe({
      next: (h) => {
        this.hotel.set(h);
        this.loading.set(false);
        this.loadSimilar(h.cityName, code);
      },
      error: () => this.loading.set(false),
    });
    this.hotelService.getRooms(code).subscribe((r) => this.rooms.set(r));
  }

  private loadSimilar(city: string, currentCode: string): void {
    this.hotelService.search({ q: city, size: 8 }).subscribe((res) => {
      const others = res.hotels.filter((h) => h.hotelCode !== currentCode).slice(0, 4);
      this.similar.set(others);
    });
  }

  toggleFavorite(): void {
    this.favoritesService.toggle(this.hotelCode());
  }

  share(): void {
    const url = window.location.href;
    if (navigator.share) {
      navigator.share({ title: this.hotel()?.name, url }).catch(() => {});
    } else {
      navigator.clipboard?.writeText(url);
      this.snackBar.open('Bağlantı kopyalandı', 'Tamam', { duration: 2000 });
    }
  }

  selectRoom(room: Room): void {
    this.selectedRoom.set(room);
  }

  readonly amenityGroups = computed<AmenityGroup[]>(() => parseAmenities(this.hotel()?.facilities));

  book(): void {
    const room = this.selectedRoom();
    const inD = this.checkInSignal();
    const outD = this.checkOutSignal();

    if (!room || !inD || !outD || this.nights() < 1) {
      this.snackBar.open('Lütfen oda ve geçerli tarih aralığı seçin', 'Tamam', { duration: 3000 });
      return;
    }

    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login'], {
        queryParams: { returnUrl: `/hotels/${this.hotelCode()}` },
      });
      return;
    }

    this.booking.set(true);
    this.reservationService
      .create({
        roomId: room.id,
        checkIn: this.toIsoDate(inD),
        checkOut: this.toIsoDate(outD),
      })
      .subscribe({
        next: () => {
          this.booking.set(false);
          this.snackBar.open('Rezervasyon oluşturuldu!', 'Tamam', { duration: 3000 });
          this.router.navigate(['/reservations']);
        },
        error: (err: HttpErrorResponse) => {
          this.booking.set(false);
          const msg = err.error?.message ?? 'Rezervasyon başarısız oldu';
          this.snackBar.open(msg, 'Tamam', { duration: 4000 });
        },
      });
  }

  private toIsoDate(d: Date): string {
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
