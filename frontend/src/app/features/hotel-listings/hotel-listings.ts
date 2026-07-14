import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HotelListingService } from '../../core/services/hotel-listing.service';
import { HotelListing } from '../../core/models/hotel-listing.model';

@Component({
  selector: 'app-hotel-listings',
  imports: [RouterLink, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './hotel-listings.html',
  styleUrl: './hotel-listings.scss',
})
export class HotelListings {
  private readonly listingService = inject(HotelListingService);
  private readonly snackBar = inject(MatSnackBar);

  readonly listings = signal<HotelListing[]>([]);
  readonly loading = signal(true);
  readonly processingId = signal<number | null>(null);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.listingService.getPending().subscribe({
      next: (list) => {
        this.listings.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  approve(item: HotelListing): void {
    this.processingId.set(item.id);
    this.listingService.approve(item.id).subscribe({
      next: () => {
        this.processingId.set(null);
        this.snackBar.open(`${item.name} onaylandı, aramada görünecek`, 'Tamam', { duration: 3000 });
        this.load();
      },
      error: () => {
        this.processingId.set(null);
        this.snackBar.open('İşlem başarısız', 'Tamam', { duration: 3000 });
      },
    });
  }

  reject(item: HotelListing): void {
    this.processingId.set(item.id);
    this.listingService.reject(item.id).subscribe({
      next: () => {
        this.processingId.set(null);
        this.snackBar.open(`${item.name} reddedildi`, 'Tamam', { duration: 3000 });
        this.load();
      },
      error: () => {
        this.processingId.set(null);
        this.snackBar.open('İşlem başarısız', 'Tamam', { duration: 3000 });
      },
    });
  }
}
