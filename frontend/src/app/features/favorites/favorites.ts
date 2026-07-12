import { Component, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FavoritesService } from '../../core/services/favorites.service';
import { HotelService } from '../../core/services/hotel.service';
import { HotelSummary } from '../../core/models/hotel.model';
import { HotelCard } from '../../shared/components/hotel-card/hotel-card';

@Component({
  selector: 'app-favorites',
  imports: [RouterLink, MatIconModule, MatProgressSpinnerModule, HotelCard],
  templateUrl: './favorites.html',
  styleUrl: './favorites.scss',
})
export class Favorites {
  private readonly favoritesService = inject(FavoritesService);
  private readonly hotelService = inject(HotelService);

  readonly hotels = signal<HotelSummary[]>([]);
  readonly loading = signal(true);

  constructor() {
    // favoriler degistikce (kart uzerinden cikarma vb.) listeyi tazele
    effect(() => {
      const codes = this.favoritesService.favorites();
      this.loadHotels(codes);
    });
  }

  private loadHotels(codes: string[]): void {
    if (codes.length === 0) {
      this.hotels.set([]);
      this.loading.set(false);
      return;
    }

    this.loading.set(true);
    const requests = codes.map((code) =>
      this.hotelService.getByHotelCode(code).pipe(
        map(
          (h): HotelSummary => ({
            hotelCode: h.hotelCode,
            name: h.name,
            countryName: h.countryName,
            cityName: h.cityName,
            rating: h.rating,
          }),
        ),
        catchError(() => of(null)),
      ),
    );

    forkJoin(requests).subscribe((results) => {
      this.hotels.set(results.filter((h): h is HotelSummary => h !== null));
      this.loading.set(false);
    });
  }
}
