import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HostApplicationService } from '../../core/services/host-application.service';
import { HostApplication } from '../../core/models/host-application.model';

@Component({
  selector: 'app-host-applications',
  imports: [DatePipe, RouterLink, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './host-applications.html',
  styleUrl: './host-applications.scss',
})
export class HostApplications {
  private readonly applicationService = inject(HostApplicationService);
  private readonly snackBar = inject(MatSnackBar);

  readonly applications = signal<HostApplication[]>([]);
  readonly loading = signal(true);
  readonly processingId = signal<number | null>(null);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.applicationService.getPending().subscribe({
      next: (list) => {
        this.applications.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  approve(app: HostApplication): void {
    this.processingId.set(app.id);
    this.applicationService.approve(app.id).subscribe({
      next: () => {
        this.processingId.set(null);
        this.snackBar.open(`${app.firstName} ${app.lastName} onaylandı`, 'Tamam', { duration: 3000 });
        this.load();
      },
      error: () => {
        this.processingId.set(null);
        this.snackBar.open('İşlem başarısız', 'Tamam', { duration: 3000 });
      },
    });
  }

  reject(app: HostApplication): void {
    this.processingId.set(app.id);
    this.applicationService.reject(app.id).subscribe({
      next: () => {
        this.processingId.set(null);
        this.snackBar.open(`${app.firstName} ${app.lastName} reddedildi`, 'Tamam', { duration: 3000 });
        this.load();
      },
      error: () => {
        this.processingId.set(null);
        this.snackBar.open('İşlem başarısız', 'Tamam', { duration: 3000 });
      },
    });
  }
}
