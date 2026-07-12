import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// Sadece platform yöneticisi (SUPER_ADMIN) erişebilir; değilse ana sayfaya yönlendirir.
export const superAdminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn() && authService.isSuperAdmin()) {
    return true;
  }

  router.navigate(['/']);
  return false;
};
