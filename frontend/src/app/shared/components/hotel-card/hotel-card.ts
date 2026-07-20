import { Component, computed, inject, input, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { HotelSummary } from '../../../core/models/hotel.model';
import { FavoritesService } from '../../../core/services/favorites.service';
import { StarRating } from '../star-rating/star-rating';
import {
  demoReviewCount,
  demoReviewScore,
  demoTags,
  hotelPhoto,
  reviewScoreLabel,
} from '../../../core/utils/hotel-visuals';

@Component({
  selector: 'app-hotel-card',
  imports: [DecimalPipe, RouterLink, MatIconModule, StarRating],
  templateUrl: './hotel-card.html',
  styleUrl: './hotel-card.scss',
})
export class HotelCard {
  readonly hotel = input.required<HotelSummary>();

  readonly favorites = inject(FavoritesService);

  readonly photo = computed(() => hotelPhoto(this.hotel().hotelCode));
  // Gercek en ucuz oda fiyati (backend'den). Kart = detay = rezervasyon tutarli.
  readonly price = computed(() => this.hotel().minPrice);
  readonly reviewCount = computed(() => demoReviewCount(this.hotel().hotelCode));
  readonly reviewScore = computed(() => demoReviewScore(this.hotel().hotelCode));
  readonly reviewLabel = computed(() => reviewScoreLabel(this.reviewScore()));
  readonly tags = computed(() => demoTags(this.hotel().hotelCode));

  readonly imgFailed = signal(false);

  onImgError(): void {
    this.imgFailed.set(true);
  }

  toggleFavorite(event: Event): void {
    // kart link oldugu icin tiklamanin detaya gitmesini engelle
    event.preventDefault();
    event.stopPropagation();
    this.favorites.toggle(this.hotel().hotelCode);
  }
}
