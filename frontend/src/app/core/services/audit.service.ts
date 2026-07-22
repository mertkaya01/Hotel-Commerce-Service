import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuditLogPage } from '../models/audit.model';

@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/admin/audit-logs`;

  list(opts: { type?: string; actor?: string; page?: number; size?: number }): Observable<AuditLogPage> {
    let params = new HttpParams()
      .set('page', String(opts.page ?? 0))
      .set('size', String(opts.size ?? 20));
    if (opts.type) params = params.set('type', opts.type);
    if (opts.actor) params = params.set('actor', opts.actor);
    return this.http.get<AuditLogPage>(this.apiUrl, { params });
  }

  eventTypes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/types`);
  }
}
