import { Component, computed, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/**
 * Booking.com tarzı misafir seçici: stepper'lı dropdown (Yetişkin/Çocuk/Oda) +
 * "Evcil hayvan" checkbox'ı. Görsel/UX amaçlı — değerler backend aramasına gitmez.
 */
@Component({
  selector: 'app-guest-selector',
  imports: [MatIconModule],
  templateUrl: './guest-selector.html',
  styleUrl: './guest-selector.scss',
})
export class GuestSelector {
  readonly open = signal(false);

  readonly adults = signal(2);
  readonly children = signal(0);
  readonly rooms = signal(1);
  readonly pet = signal(false);

  readonly summary = computed(
    () => `${this.adults()} Yetişkin · ${this.children()} Çocuk · ${this.rooms()} Oda`,
  );

  toggle(): void {
    this.open.update((v) => !v);
  }

  close(): void {
    this.open.set(false);
  }

  step(field: 'adults' | 'children' | 'rooms', delta: number): void {
    const min = field === 'children' ? 0 : 1;
    const target = { adults: this.adults, children: this.children, rooms: this.rooms }[field];
    target.set(Math.max(min, Math.min(12, target() + delta)));
  }

  togglePet(): void {
    this.pet.update((v) => !v);
  }
}
