import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/services/auth.service';
import { HotelListingService } from '../../core/services/hotel-listing.service';
import { HotelListing } from '../../core/models/hotel-listing.model';
import { resolveFileUrl } from '../../core/utils/file-url';

@Component({
  selector: 'app-host',
  imports: [RouterLink, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './host.html',
  styleUrl: './host.scss',
})
export class Host {
  private readonly authService = inject(AuthService);
  private readonly listingService = inject(HotelListingService);

  readonly user = this.authService.currentUser;
  readonly listings = signal<HotelListing[]>([]);
  readonly loading = signal(true);

  readonly approvedCount = computed(
    () => this.listings().filter((l) => l.status === 'APPROVED').length,
  );
  readonly roomCount = computed(() => this.listings().reduce((sum, l) => sum + l.roomCount, 0));
  readonly pendingCount = computed(
    () => this.listings().filter((l) => l.status === 'PENDING').length,
  );

  ngOnInit(): void {
    this.listingService.getMyListings().subscribe({
      next: (list) => {
        this.listings.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  statusLabel(status: string): string {
    return status === 'APPROVED' ? 'Yayında' : status === 'PENDING' ? 'Onay Bekliyor' : 'Reddedildi';
  }

  /** Yüklenen fotoğrafların göreceli yolunu tam adrese çevirir. */
  photoUrl(url: string): string {
    return resolveFileUrl(url);
  }
}
