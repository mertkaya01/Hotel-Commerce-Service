import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReservationService } from '../../core/services/reservation.service';
import { Reservation } from '../../core/models/reservation.model';

@Component({
  selector: 'app-reservations',
  imports: [
    DatePipe,
    DecimalPipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './reservations.html',
  styleUrl: './reservations.scss',
})
export class Reservations {
  private readonly reservationService = inject(ReservationService);
  private readonly snackBar = inject(MatSnackBar);

  readonly reservations = signal<Reservation[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.reservationService.getMyReservations().subscribe({
      next: (list) => {
        // en yeni rezervasyon en ustte olsun
        this.reservations.set([...list].sort((a, b) => b.id - a.id));
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  cancel(reservation: Reservation): void {
    this.reservationService.cancel(reservation.id).subscribe({
      next: () => {
        this.snackBar.open('Rezervasyon iptal edildi', 'Tamam', { duration: 3000 });
        this.load();
      },
      error: () => this.snackBar.open('İptal başarısız oldu', 'Tamam', { duration: 3000 }),
    });
  }
}
