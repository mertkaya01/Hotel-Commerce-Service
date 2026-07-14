import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HotelListingService } from '../../core/services/hotel-listing.service';
import { HotelListingRequest } from '../../core/models/hotel-listing.model';
import { SELECTABLE_AMENITIES } from '../../core/utils/amenities';

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
    photos: this.fb.array([this.newPhotoControl()]),
    rooms: this.fb.array([this.newRoomGroup()]),
  });

  get photos(): FormArray {
    return this.form.get('photos') as FormArray;
  }

  get rooms(): FormArray {
    return this.form.get('rooms') as FormArray;
  }

  private newPhotoControl(): FormControl {
    return this.fb.control('', [Validators.required]);
  }

  private newRoomGroup() {
    return this.fb.group({
      roomType: ['DOUBLE', [Validators.required]],
      capacity: [2, [Validators.required, Validators.min(1)]],
      pricePerNight: [null, [Validators.required, Validators.min(1)]],
    });
  }

  addPhoto(): void {
    this.photos.push(this.newPhotoControl());
  }

  removePhoto(i: number): void {
    if (this.photos.length > 1) this.photos.removeAt(i);
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

    const raw = this.form.getRawValue();
    const request: HotelListingRequest = {
      name: raw.name!,
      countryName: raw.countryName!,
      cityName: raw.cityName!,
      rating: raw.rating!,
      address: raw.address ?? '',
      description: raw.description!,
      amenities: Array.from(this.selectedAmenities()),
      photos: (raw.photos as string[]).map((p) => p.trim()).filter((p) => p.length > 0),
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
