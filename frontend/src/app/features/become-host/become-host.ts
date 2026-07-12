import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HostApplicationService } from '../../core/services/host-application.service';

@Component({
  selector: 'app-become-host',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './become-host.html',
  styleUrl: './become-host.scss',
})
export class BecomeHost {
  private readonly fb = inject(FormBuilder);
  private readonly applicationService = inject(HostApplicationService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly todayStr = new Date().toISOString().slice(0, 10);

  readonly form = this.fb.group({
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    birthDate: ['', [Validators.required]],
    description: ['', [Validators.required, Validators.minLength(20)]],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.applicationService.apply(this.form.getRawValue() as any).subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open('Başvurun alındı! Değerlendirme sonrası bilgilendirileceksin.', 'Tamam', {
          duration: 4000,
        });
        this.router.navigate(['/profile']);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set(err.error?.message ?? 'Başvuru gönderilemedi');
      },
    });
  }
}
