import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HostApplicationService } from '../../core/services/host-application.service';
import { HostApplication } from '../../core/models/host-application.model';
import { HotelListingService } from '../../core/services/hotel-listing.service';
import { HotelListing } from '../../core/models/hotel-listing.model';
import { resolveFileUrl } from '../../core/utils/file-url';

@Component({
  selector: 'app-host-applications',
  imports: [DatePipe, RouterLink, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './host-applications.html',
  styleUrl: './host-applications.scss',
})
export class HostApplications {
  private readonly applicationService = inject(HostApplicationService);
  private readonly listingService = inject(HotelListingService);
  private readonly snackBar = inject(MatSnackBar);

  readonly applications = signal<HostApplication[]>([]);
  readonly hotels = signal<HotelListing[]>([]);
  readonly loading = signal(true);
  readonly processingId = signal<number | null>(null);
  readonly processingHotelId = signal<number | null>(null);

  ngOnInit(): void {
    this.load();
    this.loadHotels();
  }

  /** Yüklenen fotoğrafların göreceli yolunu tam adrese çevirir. */
  photoUrl(url: string): string {
    return resolveFileUrl(url);
  }

  private load(): void {
    this.loading.set(true);
    this.applicationService.getPending().subscribe({
      next: (list) => {
        this.applications.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadHotels(): void {
    this.listingService.getPending().subscribe({
      next: (list) => this.hotels.set(list),
      error: () => this.hotels.set([]),
    });
  }

  approveHotel(item: HotelListing): void {
    this.processingHotelId.set(item.id);
    this.listingService.approve(item.id).subscribe({
      next: () => {
        this.processingHotelId.set(null);
        this.snackBar.open(`${item.name} onaylandı, aramada görünecek`, 'Tamam', { duration: 3000 });
        this.loadHotels();
      },
      error: () => {
        this.processingHotelId.set(null);
        this.snackBar.open('İşlem başarısız', 'Tamam', { duration: 3000 });
      },
    });
  }

  rejectHotel(item: HotelListing): void {
    this.processingHotelId.set(item.id);
    this.listingService.reject(item.id).subscribe({
      next: () => {
        this.processingHotelId.set(null);
        this.snackBar.open(`${item.name} reddedildi`, 'Tamam', { duration: 3000 });
        this.loadHotels();
      },
      error: () => {
        this.processingHotelId.set(null);
        this.snackBar.open('İşlem başarısız', 'Tamam', { duration: 3000 });
      },
    });
  }

  approve(app: HostApplication): void {
    this.processingId.set(app.id);
    this.applicationService.approve(app.id).subscribe({
      next: () => {
        this.processingId.set(null);
        this.snackBar.open(`${app.firstName} ${app.lastName} onaylandı`, 'Tamam', { duration: 3000 });
        this.load();
      },
      error: () => {
        this.processingId.set(null);
        this.snackBar.open('İşlem başarısız', 'Tamam', { duration: 3000 });
      },
    });
  }

  reject(app: HostApplication): void {
    this.processingId.set(app.id);
    this.applicationService.reject(app.id).subscribe({
      next: () => {
        this.processingId.set(null);
        this.snackBar.open(`${app.firstName} ${app.lastName} reddedildi`, 'Tamam', { duration: 3000 });
        this.load();
      },
      error: () => {
        this.processingId.set(null);
        this.snackBar.open('İşlem başarısız', 'Tamam', { duration: 3000 });
      },
    });
  }
}
