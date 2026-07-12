import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// Sadece ev sahibi (ADMIN) erişebilir; değilse profile yönlendirir.
export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn() && authService.isHost()) {
    return true;
  }

  router.navigate(['/profile']);
  return false;
};
