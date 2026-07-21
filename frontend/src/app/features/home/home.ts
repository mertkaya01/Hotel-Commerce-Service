import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { HotelService } from '../../core/services/hotel.service';
import { SearchDatesService } from '../../core/services/search-dates.service';
import {
  FacetValue,
  HotelSearchParams,
  HotelSort,
  HotelSummary,
} from '../../core/models/hotel.model';
import { HotelCard } from '../../shared/components/hotel-card/hotel-card';
import { FeatureCard } from '../../shared/components/feature-card/feature-card';
import { Faq } from '../../shared/components/faq/faq';
import { GuestSelector } from '../../shared/components/guest-selector/guest-selector';
import { resolveSearchTerm } from '../../core/utils/search-terms';

interface ActiveFilters {
  country?: string;
  city?: string;
  rating?: string;
}

interface PopularCity {
  label: string; // kartta gösterilen ad
  query: string; // aramada kullanılan (veride geçen) şehir adı
  photo: string;
}

const PAGE_SIZE = 12;

const POPULAR_CITIES: PopularCity[] = [
  { label: 'İstanbul', query: 'Istanbul', photo: 'photo-1541432901042-2d8bd64b4a9b' },
  { label: 'Antalya', query: 'Antalya', photo: 'photo-1589561253898-768105ca91a8' },
  { label: 'Kapadokya', query: 'Nevsehir', photo: 'photo-1641128324972-af3212f0f6bd' },
  { label: 'Bodrum', query: 'Bodrum', photo: 'photo-1519046904884-53103b34b206' },
  { label: 'İzmir', query: 'Izmir', photo: 'photo-1533105079780-92b9be482077' },
];

const WHY_US = [
  { icon: 'payments', title: 'En İyi Fiyat', text: 'Binlerce oteli karşılaştır, en uygun fiyatı yakala.' },
  { icon: 'event_available', title: 'Ücretsiz İptal', text: 'Çoğu rezervasyonda esnek iptal imkânı.' },
  { icon: 'support_agent', title: '7/24 Destek', text: 'Sorularında her an yanındayız.' },
  { icon: 'verified_user', title: 'Güvenli Ödeme', text: 'Verilerin güvende, işlemlerin korumalı.' },
];

const RATING_LABELS: Record<string, string> = {
  FIVE_STAR: '5 Yıldız',
  FOUR_STAR: '4 Yıldız',
  THREE_STAR: '3 Yıldız',
  TWO_STAR: '2 Yıldız',
  ONE_STAR: '1 Yıldız',
  UNRATED: 'Değerlendirilmemiş',
};

@Component({
  selector: 'app-home',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule,
    HotelCard,
    FeatureCard,
    Faq,
    GuestSelector,
  ],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  private readonly hotelService = inject(HotelService);
  private readonly searchDates = inject(SearchDatesService);

  // Ana arama alanı — backend'e giden gerçek parametre (q)
  readonly searchControl = new FormControl('', { nonNullable: true });

  // Zengin arama paneli alanları. NOT: tarih/misafir/seyahat türü şu an
  // yalnızca UI/UX içindir — backend araması bu alanlara göre filtrelemiyor.
  // (İleride oda müsaitliği + kapasite filtresi olarak bağlanabilir.)
  readonly todayStr = new Date().toISOString().slice(0, 10);
  readonly checkIn = new FormControl('', { nonNullable: true });
  readonly checkOut = new FormControl('', { nonNullable: true });

  readonly featured = signal<HotelSummary[]>([]);
  readonly results = signal<HotelSummary[]>([]);
  readonly facets = signal<Record<string, FacetValue[]>>({});
  readonly totalResults = signal(0);
  readonly page = signal(0);
  readonly loading = signal(false);
  readonly hasSearched = signal(false);
  readonly sort = signal<HotelSort>('relevance');

  // NOT: fiyata göre sıralama yok — fiyatlar demo olarak arayüzde üretiliyor,
  // Solr'da fiyat alanı olmadığı için 5000 otel genelinde sıralanamaz.
  readonly sortOptions: { value: HotelSort; label: string }[] = [
    { value: 'relevance', label: 'En İlgili' },
    { value: 'rating_desc', label: 'Yıldız (yüksek → düşük)' },
    { value: 'rating_asc', label: 'Yıldız (düşük → yüksek)' },
    { value: 'name_asc', label: 'İsim (A → Z)' },
    { value: 'name_desc', label: 'İsim (Z → A)' },
  ];

  // Fiyat aralığı filtresi (otelin en ucuz oda gecelik fiyatına göre)
  readonly priceMin = signal<number | null>(null);
  readonly priceMax = signal<number | null>(null);
  readonly pricePresets: { label: string; min: number | null; max: number | null }[] = [
    { label: '0 – 100 ₺', min: 0, max: 100 },
    { label: '100 – 200 ₺', min: 100, max: 200 },
    { label: '200 – 300 ₺', min: 200, max: 300 },
    { label: '300 ₺ +', min: 300, max: null },
  ];

  private query = '';
  readonly activeFilters = signal<ActiveFilters>({});

  readonly ratingLabels = RATING_LABELS;
  readonly popularCities = POPULAR_CITIES;
  readonly whyUs = WHY_US;

  constructor() {
    this.loadFeatured();
  }

  cityPhoto(photoId: string): string {
    return `https://images.unsplash.com/${photoId}?w=500&h=360&fit=crop&q=80`;
  }

  // arama sonuçlarında gösterilecek "gg.aa.yyyy - gg.aa.yyyy" etiketi (tarih seçildiyse)
  get selectedDatesLabel(): string | null {
    const inV = this.checkIn.value;
    const outV = this.checkOut.value;
    if (!inV || !outV) return null;
    const fmt = (s: string) => s.split('-').reverse().join('.');
    return `${fmt(inV)} - ${fmt(outV)}`;
  }

  searchCity(city: PopularCity): void {
    this.searchControl.setValue(city.query);
    this.onSearch();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  private loadFeatured(): void {
    this.hotelService.search({ rating: 'FIVE_STAR', size: 8 }).subscribe({
      next: (res) => {
        if (res.hotels.length > 0) {
          this.featured.set(res.hotels);
        } else {
          this.hotelService
            .search({ rating: 'FOUR_STAR', size: 8 })
            .subscribe((r) => this.featured.set(r.hotels));
        }
      },
    });
  }

  onSearch(): void {
    // Bilinen bir ülke/şehir adı yazıldıysa serbest metin yerine FİLTRE uygula:
    // "almanya" -> country=Germany (yoksa açıklamada "Germany" geçen Hollanda
    // otelleri de gelir). Aksi halde serbest metin araması yapılır.
    const resolved = resolveSearchTerm(this.searchControl.value);
    this.query = '';
    const filters: ActiveFilters = {};
    if (resolved.type === 'country') filters.country = resolved.value;
    else if (resolved.type === 'city') filters.city = resolved.value;
    else this.query = resolved.value;

    // seçilen tarih aralığını sakla -> otel detayındaki booking kutusuna taşınır
    this.searchDates.set(this.checkIn.value, this.checkOut.value);
    this.activeFilters.set(filters);
    this.priceMin.set(null);
    this.priceMax.set(null);
    this.page.set(0);
    this.hasSearched.set(true);
    this.runSearch();
  }

  applyPricePreset(min: number | null, max: number | null): void {
    this.priceMin.set(min);
    this.priceMax.set(max);
    this.page.set(0);
    this.runSearch();
  }

  applyPriceInputs(minRaw: string, maxRaw: string): void {
    const min = minRaw !== '' ? Math.max(0, Number(minRaw)) : null;
    const max = maxRaw !== '' ? Math.max(0, Number(maxRaw)) : null;
    // geçersiz aralık (min > max) ise sessizce yok say
    if (min != null && max != null && min > max) return;
    this.priceMin.set(Number.isFinite(min as number) ? min : null);
    this.priceMax.set(Number.isFinite(max as number) ? max : null);
    this.page.set(0);
    this.runSearch();
  }

  clearPriceFilter(): void {
    this.priceMin.set(null);
    this.priceMax.set(null);
    this.page.set(0);
    this.runSearch();
  }

  get hasPriceFilter(): boolean {
    return this.priceMin() != null || this.priceMax() != null;
  }

  get priceFilterLabel(): string {
    const lo = this.priceMin();
    const hi = this.priceMax();
    if (lo != null && hi != null) return `${lo} – ${hi} ₺`;
    if (lo != null) return `${lo} ₺ +`;
    if (hi != null) return `0 – ${hi} ₺`;
    return '';
  }

  applyFilter(type: keyof ActiveFilters, value: string): void {
    this.activeFilters.update((f) => ({ ...f, [type]: value }));
    this.page.set(0);
    this.runSearch();
  }

  removeFilter(type: keyof ActiveFilters): void {
    this.activeFilters.update((f) => {
      const next = { ...f };
      delete next[type];
      return next;
    });
    this.page.set(0);
    this.runSearch();
  }

  onSortChange(value: HotelSort): void {
    this.sort.set(value);
    this.page.set(0); // sıralama değişince ilk sayfaya dön
    this.runSearch();
  }

  goToPage(newPage: number): void {
    this.page.set(newPage);
    this.runSearch();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  get totalPages(): number {
    return Math.ceil(this.totalResults() / PAGE_SIZE);
  }

  facetKeys(): { key: string; label: string; type: keyof ActiveFilters }[] {
    return [
      { key: 'countryName', label: 'Ülke', type: 'country' },
      { key: 'cityName', label: 'Şehir', type: 'city' },
      { key: 'rating', label: 'Yıldız', type: 'rating' },
    ];
  }

  ratingText(value: string): string {
    return RATING_LABELS[value] ?? value;
  }

  activeFilterEntries(): { type: keyof ActiveFilters; value: string }[] {
    const f = this.activeFilters();
    return (Object.keys(f) as (keyof ActiveFilters)[])
      .filter((k) => f[k])
      .map((k) => ({ type: k, value: f[k] as string }));
  }

  private runSearch(): void {
    this.loading.set(true);
    const filters = this.activeFilters();
    const params: HotelSearchParams = {
      q: this.query || undefined,
      country: filters.country,
      city: filters.city,
      rating: filters.rating,
      minPrice: this.priceMin() ?? undefined,
      maxPrice: this.priceMax() ?? undefined,
      sort: this.sort(),
      page: this.page(),
      size: PAGE_SIZE,
    };

    this.hotelService.search(params).subscribe({
      next: (res) => {
        this.results.set(res.hotels);
        this.facets.set(res.facets ?? {});
        this.totalResults.set(res.totalResults);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
