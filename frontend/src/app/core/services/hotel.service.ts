import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  HotelDetail,
  HotelSearchParams,
  HotelSearchResponse,
  Room,
} from '../models/hotel.model';

@Injectable({ providedIn: 'root' })
export class HotelService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/hotels`;

  search(params: HotelSearchParams): Observable<HotelSearchResponse> {
    let httpParams = new HttpParams();
    if (params.q) httpParams = httpParams.set('q', params.q);
    if (params.country) httpParams = httpParams.set('country', params.country);
    if (params.city) httpParams = httpParams.set('city', params.city);
    if (params.rating) httpParams = httpParams.set('rating', params.rating);
    httpParams = httpParams.set('page', String(params.page ?? 0));
    httpParams = httpParams.set('size', String(params.size ?? 12));

    return this.http.get<HotelSearchResponse>(`${this.apiUrl}/search`, { params: httpParams });
  }

  getByHotelCode(hotelCode: string): Observable<HotelDetail> {
    return this.http.get<HotelDetail>(`${this.apiUrl}/${hotelCode}`);
  }

  getRooms(hotelCode: string): Observable<Room[]> {
    return this.http.get<Room[]>(`${this.apiUrl}/${hotelCode}/rooms`);
  }
}
