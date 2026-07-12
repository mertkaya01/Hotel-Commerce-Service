import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-feature-card',
  imports: [MatIconModule],
  template: `
    <div class="feature-card">
      <div class="icon"><mat-icon>{{ icon() }}</mat-icon></div>
      <h4>{{ title() }}</h4>
      <p>{{ text() }}</p>
    </div>
  `,
  styleUrl: './feature-card.scss',
})
export class FeatureCard {
  readonly icon = input.required<string>();
  readonly title = input.required<string>();
  readonly text = input<string>('');
}
