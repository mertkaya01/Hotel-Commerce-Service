import { Component, input } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { DemoReview } from '../../../core/utils/hotel-visuals';

@Component({
  selector: 'app-review-card',
  imports: [DecimalPipe],
  template: `
    <article class="review-card">
      <header>
        <span class="avatar">{{ review().initial }}</span>
        <div class="who">
          <strong>{{ review().name }}</strong>
          <span class="date">{{ review().date }}</span>
        </div>
        <span class="score">{{ review().score | number: '1.1-1' }}</span>
      </header>
      <p class="comment">{{ review().comment }}</p>
    </article>
  `,
  styleUrl: './review-card.scss',
})
export class ReviewCard {
  readonly review = input.required<DemoReview>();
}
