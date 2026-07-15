import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HotelListingService } from '../../core/services/hotel-listing.service';
import { HotelListingRequest } from '../../core/models/hotel-listing.model';
import { SELECTABLE_AMENITIES } from '../../core/utils/amenities';
import { DEMO_HOTEL_PHOTOS } from '../../core/utils/hotel-visuals';
import { resolveFileUrl } from '../../core/utils/file-url';

@Component({
  selector: 'app-add-hotel',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './add-hotel.html',
  styleUrl: './add-hotel.scss',
})
export class AddHotel {
  private readonly fb = inject(FormBuilder);
  private readonly listingService = inject(HotelListingService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly amenityOptions = SELECTABLE_AMENITIES;
  readonly selectedAmenities = signal<Set<string>>(new Set());

  // Fotoğraf: ya bilgisayardan yükle ya da hazır demo galeriden seç
  readonly demoPhotos = DEMO_HOTEL_PHOTOS;
  readonly selectedPhotos = signal<Set<string>>(new Set());
  readonly uploadedPhotos = signal<string[]>([]);
  readonly uploading = signal(false);

  /** Önizleme için: /uploads/... yolunu tam adrese çevirir. */
  previewUrl(url: string): string {
    return resolveFileUrl(url);
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files ?? []);
    if (files.length === 0) return;

    this.uploading.set(true);
    this.errorMessage.set(null);

    // Dosyaları sırayla yükle, dönen URL'leri seçili fotoğraflara ekle
    forkJoin(files.map((f) => this.listingService.uploadPhoto(f))).subscribe({
      next: (responses) => {
        const urls = responses.map((r) => r.url);
        this.uploadedPhotos.update((list) => [...list, ...urls]);
        this.selectedPhotos.update((set) => {
          const next = new Set(set);
          urls.forEach((u) => next.add(u));
          return next;
        });
        this.uploading.set(false);
        input.value = ''; // aynı dosya tekrar seçilebilsin
      },
      error: (err: HttpErrorResponse) => {
        this.uploading.set(false);
        this.errorMessage.set(err.error?.message ?? 'Fotoğraf yüklenemedi');
        input.value = '';
      },
    });
  }

  removeUploaded(url: string): void {
    this.uploadedPhotos.update((list) => list.filter((u) => u !== url));
    this.selectedPhotos.update((set) => {
      const next = new Set(set);
      next.delete(url);
      return next;
    });
  }

  readonly ratingOptions = [
    { value: 'FIVE_STAR', label: '5 Yıldız' },
    { value: 'FOUR_STAR', label: '4 Yıldız' },
    { value: 'THREE_STAR', label: '3 Yıldız' },
    { value: 'TWO_STAR', label: '2 Yıldız' },
    { value: 'ONE_STAR', label: '1 Yıldız' },
  ];
  readonly roomTypes = ['SINGLE', 'DOUBLE', 'SUITE', 'DELUXE'];

  readonly form = this.fb.group({
    name: ['', [Validators.required]],
    countryName: ['', [Validators.required]],
    cityName: ['', [Validators.required]],
    rating: ['FOUR_STAR', [Validators.required]],
    address: [''],
    description: ['', [Validators.required, Validators.minLength(20)]],
    rooms: this.fb.array([this.newRoomGroup()]),
  });

  get rooms(): FormArray {
    return this.form.get('rooms') as FormArray;
  }

  togglePhoto(url: string): void {
    const next = new Set(this.selectedPhotos());
    next.has(url) ? next.delete(url) : next.add(url);
    this.selectedPhotos.set(next);
  }

  isPhotoSelected(url: string): boolean {
    return this.selectedPhotos().has(url);
  }

  private newRoomGroup() {
    return this.fb.group({
      roomType: ['DOUBLE', [Validators.required]],
      capacity: [2, [Validators.required, Validators.min(1)]],
      pricePerNight: [null, [Validators.required, Validators.min(1)]],
    });
  }

  addRoom(): void {
    this.rooms.push(this.newRoomGroup());
  }

  removeRoom(i: number): void {
    if (this.rooms.length > 1) this.rooms.removeAt(i);
  }

  toggleAmenity(key: string): void {
    const next = new Set(this.selectedAmenities());
    next.has(key) ? next.delete(key) : next.add(key);
    this.selectedAmenities.set(next);
  }

  isAmenitySelected(key: string): boolean {
    return this.selectedAmenities().has(key);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Lütfen zorunlu alanları doldurun.');
      return;
    }

    if (this.selectedPhotos().size === 0) {
      this.errorMessage.set('En az bir fotoğraf seç.');
      return;
    }

    const raw = this.form.getRawValue();
    const request: HotelListingRequest = {
      name: raw.name!,
      countryName: raw.countryName!,
      cityName: raw.cityName!,
      rating: raw.rating!,
      address: raw.address ?? '',
      description: raw.description!,
      amenities: Array.from(this.selectedAmenities()),
      photos: Array.from(this.selectedPhotos()),
      rooms: (raw.rooms as any[]).map((r) => ({
        roomType: r.roomType,
        capacity: Number(r.capacity),
        pricePerNight: Number(r.pricePerNight),
      })),
    };

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.listingService.submit(request).subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open('Otelin gönderildi! Onaydan sonra aramada görünecek.', 'Tamam', {
          duration: 4000,
        });
        this.router.navigate(['/host']);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set(err.error?.message ?? 'Otel gönderilemedi');
      },
    });
  }
}
