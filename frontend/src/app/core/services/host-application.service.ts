import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { HostApplication, HostApplicationRequest } from '../models/host-application.model';

@Injectable({ providedIn: 'root' })
export class HostApplicationService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/host-applications`;

  apply(request: HostApplicationRequest): Observable<HostApplication> {
    return this.http.post<HostApplication>(this.apiUrl, request);
  }

  getMyApplication(): Observable<HostApplication | null> {
    return this.http.get<HostApplication | null>(`${this.apiUrl}/me`);
  }

  // Admin
  getPending(): Observable<HostApplication[]> {
    return this.http.get<HostApplication[]>(`${this.apiUrl}/pending`);
  }

  approve(id: number): Observable<HostApplication> {
    return this.http.post<HostApplication>(`${this.apiUrl}/${id}/approve`, {});
  }

  reject(id: number): Observable<HostApplication> {
    return this.http.post<HostApplication>(`${this.apiUrl}/${id}/reject`, {});
  }
}
