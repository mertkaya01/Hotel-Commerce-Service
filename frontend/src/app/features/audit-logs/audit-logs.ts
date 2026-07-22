import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuditService } from '../../core/services/audit.service';
import { AuditLog } from '../../core/models/audit.model';

// Olay tipi -> Türkçe etiket + renk sınıfı (chip)
const EVENT_META: Record<string, { label: string; cls: string }> = {
  USER_REGISTERED: { label: 'Üye kaydı', cls: 'ok' },
  USER_LOGIN_SUCCESS: { label: 'Giriş', cls: 'info' },
  USER_LOGIN_FAILED: { label: 'Başarısız giriş', cls: 'danger' },
  PASSWORD_CHANGED: { label: 'Şifre değişti', cls: 'warn' },
  EMAIL_VERIFIED: { label: 'E-posta doğrulandı', cls: 'ok' },
  HOTEL_SUBMITTED: { label: 'Otel eklendi', cls: 'info' },
  HOTEL_APPROVED: { label: 'Otel onaylandı', cls: 'ok' },
  HOTEL_REJECTED: { label: 'Otel reddedildi', cls: 'danger' },
  RESERVATION_CREATED: { label: 'Rezervasyon', cls: 'info' },
  RESERVATION_CANCELLED: { label: 'Rezervasyon iptal', cls: 'warn' },
  HOST_APPLICATION_SUBMITTED: { label: 'Ev sahibi başvurusu', cls: 'info' },
  HOST_APPLICATION_APPROVED: { label: 'Başvuru onaylandı', cls: 'ok' },
  HOST_APPLICATION_REJECTED: { label: 'Başvuru reddedildi', cls: 'danger' },
};

@Component({
  selector: 'app-audit-logs',
  imports: [DatePipe, RouterLink, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './audit-logs.html',
  styleUrl: './audit-logs.scss',
})
export class AuditLogs {
  private readonly auditService = inject(AuditService);

  readonly logs = signal<AuditLog[]>([]);
  readonly loading = signal(true);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly types = signal<string[]>([]);
  readonly typeFilter = signal('');
  readonly actorFilter = signal('');

  readonly hasFilter = computed(() => this.typeFilter() !== '' || this.actorFilter() !== '');

  ngOnInit(): void {
    this.auditService.eventTypes().subscribe({ next: (t) => this.types.set(t) });
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.auditService
      .list({
        type: this.typeFilter() || undefined,
        actor: this.actorFilter() || undefined,
        page: this.page(),
        size: 20,
      })
      .subscribe({
        next: (res) => {
          this.logs.set(res.content);
          this.totalPages.set(res.totalPages);
          this.totalElements.set(res.totalElements);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  onTypeChange(value: string): void {
    this.typeFilter.set(value);
    this.page.set(0);
    this.load();
  }

  onActorInput(value: string): void {
    this.actorFilter.set(value.trim());
    this.page.set(0);
    this.load();
  }

  clearFilters(): void {
    this.typeFilter.set('');
    this.actorFilter.set('');
    this.page.set(0);
    this.load();
  }

  goToPage(p: number): void {
    if (p < 0 || p >= this.totalPages()) return;
    this.page.set(p);
    this.load();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  label(type: string): string {
    return EVENT_META[type]?.label ?? type;
  }

  chipClass(type: string): string {
    return EVENT_META[type]?.cls ?? 'info';
  }

  typeLabel(type: string): string {
    return EVENT_META[type]?.label ?? type;
  }
}
