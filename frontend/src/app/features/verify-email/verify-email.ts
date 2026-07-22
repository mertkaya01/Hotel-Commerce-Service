import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/services/auth.service';

type VerifyState = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-verify-email',
  imports: [RouterLink, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './verify-email.html',
  styleUrl: './verify-email.scss',
})
export class VerifyEmail {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);

  readonly state = signal<VerifyState>('loading');
  readonly message = signal('');

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.state.set('error');
      this.message.set('Doğrulama bağlantısı geçersiz (token yok).');
      return;
    }

    this.authService.verifyEmail(token).subscribe({
      next: (res) => {
        this.state.set('success');
        this.message.set(res.message ?? 'E-posta adresin doğrulandı.');
      },
      error: (err) => {
        this.state.set('error');
        this.message.set(err.error?.message ?? 'Doğrulama başarısız oldu.');
      },
    });
  }
}
