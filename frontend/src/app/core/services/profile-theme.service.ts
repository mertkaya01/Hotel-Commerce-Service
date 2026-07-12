import { Injectable, computed, signal } from '@angular/core';

export interface BannerTheme {
  id: string;
  label: string;
  background: string;
}

// Kullanıcının profil banner'ı için seçebileceği hazır arka planlar
export const BANNER_THEMES: BannerTheme[] = [
  { id: 'ocean', label: 'Okyanus', background: 'linear-gradient(135deg, #006CE4 0%, #003B95 100%)' },
  { id: 'sunset', label: 'Gün Batımı', background: 'linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%)' },
  { id: 'forest', label: 'Orman', background: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)' },
  { id: 'grape', label: 'Üzüm', background: 'linear-gradient(135deg, #8e2de2 0%, #4a00e0 100%)' },
  { id: 'rose', label: 'Gül', background: 'linear-gradient(135deg, #ec008c 0%, #fc6767 100%)' },
  { id: 'night', label: 'Gece', background: 'linear-gradient(135deg, #232526 0%, #414345 100%)' },
  {
    id: 'beach',
    label: 'Sahil',
    background:
      "linear-gradient(135deg, rgba(0,108,228,0.75), rgba(0,59,149,0.7)), url('https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200&q=80') center/cover",
  },
];

const STORAGE_KEY = 'profile_banner';

/**
 * Kullanıcının profil banner tercihini localStorage'da tutar (backend gerektirmez).
 */
@Injectable({ providedIn: 'root' })
export class ProfileThemeService {
  private readonly selectedId = signal<string>(this.load());

  readonly themes = BANNER_THEMES;
  readonly currentId = this.selectedId.asReadonly();
  readonly currentBackground = computed(
    () => BANNER_THEMES.find((t) => t.id === this.selectedId())?.background ?? BANNER_THEMES[0].background,
  );

  select(id: string): void {
    this.selectedId.set(id);
    localStorage.setItem(STORAGE_KEY, id);
  }

  private load(): string {
    return localStorage.getItem(STORAGE_KEY) ?? BANNER_THEMES[0].id;
  }
}
