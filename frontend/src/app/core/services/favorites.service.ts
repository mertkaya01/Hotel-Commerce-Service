import { Injectable, computed, effect, inject, signal } from '@angular/core';
import { AuthService } from './auth.service';

const KEY_PREFIX = 'favorites_';
const GUEST_KEY = 'favorites_guest';

/**
 * Favori oteller — KULLANICIYA ÖZEL, localStorage'da tutulur.
 * Her kullanıcının favorileri kendi e-postasına göre ayrı bir anahtarda saklanır
 * (favorites_<email>). Giriş/çıkış olunca ilgili kullanıcının favorileri yüklenir,
 * böylece farklı kullanıcılar birbirinin favorilerini görmez.
 */
@Injectable({ providedIn: 'root' })
export class FavoritesService {
  private readonly authService = inject(AuthService);

  private readonly favoritesSignal = signal<Set<string>>(new Set());

  readonly favorites = computed(() => Array.from(this.favoritesSignal()));
  readonly count = computed(() => this.favoritesSignal().size);

  constructor() {
    // aktif kullanıcı değiştikçe (login/logout) o kullanıcının favorilerini yükle
    effect(() => {
      const user = this.authService.currentUser();
      this.favoritesSignal.set(this.load(this.storageKeyFor(user?.email)));
    });
  }

  isFavorite(hotelCode: string): boolean {
    return this.favoritesSignal().has(hotelCode);
  }

  toggle(hotelCode: string): void {
    const next = new Set(this.favoritesSignal());
    if (next.has(hotelCode)) {
      next.delete(hotelCode);
    } else {
      next.add(hotelCode);
    }
    this.favoritesSignal.set(next);
    this.persist(next);
  }

  private storageKeyFor(email: string | undefined): string {
    return email ? `${KEY_PREFIX}${email}` : GUEST_KEY;
  }

  private currentKey(): string {
    return this.storageKeyFor(this.authService.currentUser()?.email);
  }

  private load(key: string): Set<string> {
    try {
      const raw = localStorage.getItem(key);
      return raw ? new Set<string>(JSON.parse(raw)) : new Set();
    } catch {
      return new Set();
    }
  }

  private persist(set: Set<string>): void {
    localStorage.setItem(this.currentKey(), JSON.stringify(Array.from(set)));
  }
}
