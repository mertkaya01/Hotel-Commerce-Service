import { Injectable, computed, signal } from '@angular/core';

/**
 * Ana sayfada seçilen giriş/çıkış tarihlerini uygulama genelinde taşır.
 * Böylece kullanıcı bir otele girdiğinde booking kutusu bu tarihlerle dolu gelir.
 * Tarihler 'yyyy-MM-dd' string formatında tutulur (native date input ile uyumlu).
 */
@Injectable({ providedIn: 'root' })
export class SearchDatesService {
  private readonly checkInSignal = signal<string>('');
  private readonly checkOutSignal = signal<string>('');

  readonly checkIn = this.checkInSignal.asReadonly();
  readonly checkOut = this.checkOutSignal.asReadonly();

  readonly hasRange = computed(() => !!this.checkInSignal() && !!this.checkOutSignal());

  set(checkIn: string, checkOut: string): void {
    this.checkInSignal.set(checkIn);
    this.checkOutSignal.set(checkOut);
  }

  /** 'yyyy-MM-dd' -> Date (yerel), geçersizse null */
  get checkInDate(): Date | null {
    return this.toDate(this.checkInSignal());
  }

  get checkOutDate(): Date | null {
    return this.toDate(this.checkOutSignal());
  }

  private toDate(value: string): Date | null {
    if (!value) return null;
    const [y, m, d] = value.split('-').map(Number);
    if (!y || !m || !d) return null;
    return new Date(y, m - 1, d);
  }
}
