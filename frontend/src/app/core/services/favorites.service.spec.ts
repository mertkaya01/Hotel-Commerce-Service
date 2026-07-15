import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { FavoritesService } from './favorites.service';
import { AuthService } from './auth.service';

/**
 * Favoriler KULLANICIYA OZEL olmali: farkli kullanicilar birbirinin
 * favorilerini gormemeli (favorites_<email> anahtari).
 */
describe('FavoritesService', () => {
  const currentUser = signal<{ email: string } | null>(null);

  function createService(): FavoritesService {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [FavoritesService, { provide: AuthService, useValue: { currentUser } }],
    });
    return TestBed.inject(FavoritesService);
  }

  beforeEach(() => {
    localStorage.clear();
    currentUser.set(null);
  });

  it('favori ekler ve kaldirir (toggle)', () => {
    const service = createService();
    TestBed.tick();

    service.toggle('H1');
    expect(service.isFavorite('H1')).toBe(true);
    expect(service.count()).toBe(1);

    service.toggle('H1');
    expect(service.isFavorite('H1')).toBe(false);
    expect(service.count()).toBe(0);
  });

  it('giris yapan kullanicinin favorilerini kendi anahtarina yazar', () => {
    currentUser.set({ email: 'ali@x.com' });
    const service = createService();
    TestBed.tick();

    service.toggle('H1');

    expect(JSON.parse(localStorage.getItem('favorites_ali@x.com')!)).toEqual(['H1']);
  });

  it('farkli kullanicilar birbirinin favorilerini GORMEZ', () => {
    localStorage.setItem('favorites_ali@x.com', JSON.stringify(['H1', 'H2']));
    localStorage.setItem('favorites_veli@x.com', JSON.stringify(['H9']));

    currentUser.set({ email: 'ali@x.com' });
    const service = createService();
    TestBed.tick();
    expect(service.favorites()).toEqual(['H1', 'H2']);

    // kullanici degisince (login/logout) digerinin favorileri yuklenir
    currentUser.set({ email: 'veli@x.com' });
    TestBed.tick();
    expect(service.favorites()).toEqual(['H9']);
    expect(service.isFavorite('H1')).toBe(false);
  });

  it('giris yapilmamissa misafir anahtarini kullanir', () => {
    const service = createService();
    TestBed.tick();

    service.toggle('H5');

    expect(JSON.parse(localStorage.getItem('favorites_guest')!)).toEqual(['H5']);
  });

  it('bozuk localStorage verisinde cokmez', () => {
    localStorage.setItem('favorites_guest', 'bu-json-degil');

    const service = createService();
    TestBed.tick();

    expect(service.favorites()).toEqual([]);
  });
});
