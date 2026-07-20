import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';
import { RegisterRequest } from '../../../core/models/auth.model';

/**
 * Form-level validator: şifre ile şifre tekrar eşleşmeli. Hatayı doğrudan
 * confirmPassword KONTROLÜNE yazar — çünkü Material mat-error yalnızca ilgili
 * kontrol hatalıyken görünür (grup hatasını göstermez).
 */
export function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const pw = group.get('password')?.value;
  const confirm = group.get('confirmPassword');
  if (!confirm) return null;

  const mismatch = pw && confirm.value && pw !== confirm.value;
  const existing = confirm.errors ?? {};

  if (mismatch) {
    confirm.setErrors({ ...existing, passwordMismatch: true });
  } else if (existing['passwordMismatch']) {
    // yalnızca kendi hatamızı temizle, diğer hataları (required vb.) koru
    const { passwordMismatch, ...rest } = existing;
    confirm.setErrors(Object.keys(rest).length ? rest : null);
  }
  return null;
}

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly form = this.fb.group(
    {
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      // Backend ile aynı kural: en az 8 karakter + en az bir harf ve bir rakam
      password: [
        '',
        [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d)\S{8,}$/)],
      ],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordsMatch },
  );

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    // confirmPassword backend'e gönderilmez, sadece arayüz doğrulaması
    const { confirmPassword, ...payload } = this.form.getRawValue();
    this.authService.register(payload as RegisterRequest).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message ?? 'Kayit basarisiz oldu');
      },
    });
  }
}
