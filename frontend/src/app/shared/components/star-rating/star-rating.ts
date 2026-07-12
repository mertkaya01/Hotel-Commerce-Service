import { Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

const RATING_TO_STARS: Record<string, number> = {
  ONE_STAR: 1,
  TWO_STAR: 2,
  THREE_STAR: 3,
  FOUR_STAR: 4,
  FIVE_STAR: 5,
};

@Component({
  selector: 'app-star-rating',
  imports: [MatIconModule],
  templateUrl: './star-rating.html',
  styleUrl: './star-rating.scss',
})
export class StarRating {
  readonly rating = input<string>('UNRATED');

  readonly stars = computed(() => RATING_TO_STARS[this.rating()] ?? 0);
  readonly isUnrated = computed(() => this.stars() === 0);
}
