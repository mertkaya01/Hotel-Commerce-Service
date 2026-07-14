import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { HotelListing, HotelListingRequest } from '../models/hotel-listing.model';

@Injectable({ providedIn: 'root' })
export class HotelListingService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/hotel-listings`;

  // Ev sahibi
  submit(request: HotelListingRequest): Observable<HotelListing> {
    return this.http.post<HotelListing>(this.apiUrl, request);
  }

  getMyListings(): Observable<HotelListing[]> {
    return this.http.get<HotelListing[]>(`${this.apiUrl}/me`);
  }

  // Platform yöneticisi
  getPending(): Observable<HotelListing[]> {
    return this.http.get<HotelListing[]>(`${this.apiUrl}/pending`);
  }

  approve(id: number): Observable<HotelListing> {
    return this.http.post<HotelListing>(`${this.apiUrl}/${id}/approve`, {});
  }

  reject(id: number): Observable<HotelListing> {
    return this.http.post<HotelListing>(`${this.apiUrl}/${id}/reject`, {});
  }
}
