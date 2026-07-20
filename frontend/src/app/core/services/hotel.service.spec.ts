import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HotelService } from './hotel.service';
import { environment } from '../../../environments/environment';

describe('HotelService', () => {
  let service: HotelService;
  let httpMock: HttpTestingController;
  const searchUrl = `${environment.apiUrl}/hotels/search`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [HotelService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(HotelService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('varsayilan sayfa ve boyutu gonderir', () => {
    service.search({}).subscribe();

    const req = httpMock.expectOne((r) => r.url === searchUrl);
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('12');
    req.flush({ hotels: [], totalResults: 0, page: 0, size: 12, facets: {} });
  });

  it('siralama secildiginde sort parametresini gonderir', () => {
    service.search({ sort: 'rating_desc' }).subscribe();

    const req = httpMock.expectOne((r) => r.url === searchUrl);
    expect(req.request.params.get('sort')).toBe('rating_desc');
    req.flush({ hotels: [], totalResults: 0, page: 0, size: 12, facets: {} });
  });

  it('varsayilan siralamada (relevance) sort parametresi GONDERMEZ', () => {
    // backend zaten alaka puanina gore siraliyor; gereksiz parametre gondermeyelim
    service.search({ sort: 'relevance' }).subscribe();

    const req = httpMock.expectOne((r) => r.url === searchUrl);
    expect(req.request.params.has('sort')).toBe(false);
    req.flush({ hotels: [], totalResults: 0, page: 0, size: 12, facets: {} });
  });

  it('bos filtreleri parametre olarak gondermez', () => {
    service.search({ q: '', city: undefined, page: 2, size: 20 }).subscribe();

    const req = httpMock.expectOne((r) => r.url === searchUrl);
    expect(req.request.params.has('q')).toBe(false);
    expect(req.request.params.has('city')).toBe(false);
    expect(req.request.params.get('page')).toBe('2');
    req.flush({ hotels: [], totalResults: 0, page: 2, size: 20, facets: {} });
  });

  it('filtreleri parametre olarak gonderir', () => {
    service.search({ q: 'istanbul', country: 'Turkey', city: 'Istanbul', rating: 'FIVE_STAR' }).subscribe();

    const req = httpMock.expectOne((r) => r.url === searchUrl);
    expect(req.request.params.get('q')).toBe('istanbul');
    expect(req.request.params.get('country')).toBe('Turkey');
    expect(req.request.params.get('city')).toBe('Istanbul');
    expect(req.request.params.get('rating')).toBe('FIVE_STAR');
    req.flush({ hotels: [], totalResults: 0, page: 0, size: 12, facets: {} });
  });

  it('fiyat araligi verilince minPrice/maxPrice parametrelerini gonderir', () => {
    service.search({ minPrice: 100, maxPrice: 300 }).subscribe();

    const req = httpMock.expectOne((r) => r.url === searchUrl);
    expect(req.request.params.get('minPrice')).toBe('100');
    expect(req.request.params.get('maxPrice')).toBe('300');
    req.flush({ hotels: [], totalResults: 0, page: 0, size: 12, facets: {} });
  });

  it('fiyat verilmezse minPrice/maxPrice parametresi GONDERMEZ', () => {
    service.search({}).subscribe();

    const req = httpMock.expectOne((r) => r.url === searchUrl);
    expect(req.request.params.has('minPrice')).toBe(false);
    expect(req.request.params.has('maxPrice')).toBe(false);
    req.flush({ hotels: [], totalResults: 0, page: 0, size: 12, facets: {} });
  });

  it('otel detayini hotelCode ile ister', () => {
    service.getByHotelCode('HOST-ABC123').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/hotels/HOST-ABC123`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});
