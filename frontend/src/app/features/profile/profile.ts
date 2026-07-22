import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { FavoritesService } from '../../core/services/favorites.service';
import { HostApplicationService } from '../../core/services/host-application.service';
import { ProfileThemeService } from '../../core/services/profile-theme.service';
import { UserService } from '../../core/services/user.service';
import { HostApplication } from '../../core/models/host-application.model';

@Component({
  selector: 'app-profile',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly favoritesService = inject(FavoritesService);
  private readonly applicationService = inject(HostApplicationService);
  private readonly bannerService = inject(ProfileThemeService);
  private readonly snackBar = inject(MatSnackBar);

  readonly favorites = this.favoritesService;
  readonly isHost = this.authService.isHost;
  readonly isSuperAdmin = this.authService.isSuperAdmin;
  readonly user = this.authService.currentUser;

  // banner özelleştirme
  readonly banner = this.bannerService;
  readonly showThemes = signal(false);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly changingPw = signal(false);
  readonly email = signal('');
  readonly application = signal<HostApplication | null>(null);
  // e-posta doğrulama durumu (yüklenene kadar true -> uyarı yanıp sönmesin)
  readonly emailVerified = signal(true);
  readonly resending = signal(false);

  readonly initials = computed(() => {
    const u = this.user();
    if (!u) return '?';
    return ((u.firstName?.[0] ?? '') + (u.lastName?.[0] ?? '')).toUpperCase();
  });

  readonly form = this.fb.group({
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
  });

  readonly passwordForm = this.fb.group({
    currentPassword: ['', [Validators.required]],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
  });

  ngOnInit(): void {
    this.userService.getProfile().subscribe({
      next: (profile) => {
        this.email.set(profile.email);
        this.emailVerified.set(profile.emailVerified);
        if (profile.emailVerified) this.authService.markVerifiedInSession();
        this.form.patchValue({ firstName: profile.firstName, lastName: profile.lastName });
        this.authService.updateCurrentUserRole(profile.role);
        this.loading.set(false);
        // yalnızca normal kullanıcı için başvuru durumunu çek
        if (profile.role === 'USER') {
          this.loadApplication();
        }
      },
      error: () => this.loading.set(false),
    });
  }

  private loadApplication(): void {
    this.applicationService.getMyApplication().subscribe({
      next: (app) => this.application.set(app),
    });
  }

  toggleThemes(): void {
    this.showThemes.update((v) => !v);
  }

  resendVerification(): void {
    this.resending.set(true);
    this.authService.resendVerification().subscribe({
      next: () => {
        this.resending.set(false);
        this.snackBar.open('Doğrulama e-postası gönderildi, gelen kutunu kontrol et', 'Tamam', {
          duration: 4000,
        });
      },
      error: () => {
        this.resending.set(false);
        this.snackBar.open('E-posta gönderilemedi, tekrar dene', 'Tamam', { duration: 3500 });
      },
    });
  }

  onSave(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const { firstName, lastName } = this.form.getRawValue();
    this.userService.updateProfile({ firstName: firstName!, lastName: lastName! }).subscribe({
      next: (profile) => {
        this.saving.set(false);
        this.authService.updateCurrentUserName(profile.firstName, profile.lastName);
        this.snackBar.open('Profil güncellendi', 'Tamam', { duration: 3000 });
      },
      error: () => {
        this.saving.set(false);
        this.snackBar.open('Güncelleme başarısız oldu', 'Tamam', { duration: 3000 });
      },
    });
  }

  onChangePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    this.changingPw.set(true);
    const { currentPassword, newPassword } = this.passwordForm.getRawValue();
    this.userService
      .changePassword({ currentPassword: currentPassword!, newPassword: newPassword! })
      .subscribe({
        next: () => {
          this.changingPw.set(false);
          this.passwordForm.reset();
          this.snackBar.open('Şifren güncellendi', 'Tamam', { duration: 3000 });
        },
        error: (err: HttpErrorResponse) => {
          this.changingPw.set(false);
          this.snackBar.open(err.error?.message ?? 'Şifre değiştirilemedi', 'Tamam', {
            duration: 3500,
          });
        },
      });
  }
}
