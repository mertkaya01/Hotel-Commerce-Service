import { Component, computed, input, signal } from '@angular/core';

@Component({
  selector: 'app-hotel-gallery',
  imports: [],
  templateUrl: './hotel-gallery.html',
  styleUrl: './hotel-gallery.scss',
})
export class HotelGallery {
  readonly photos = input.required<string[]>();
  readonly alt = input<string>('');

  private readonly activeIndex = signal(0);
  readonly mainPhoto = computed(() => this.photos()[this.activeIndex()] ?? this.photos()[0]);
  readonly thumbs = computed(() => this.photos().slice(0, 5));

  select(index: number): void {
    this.activeIndex.set(index);
  }

  isActive(index: number): boolean {
    return this.activeIndex() === index;
  }
}
