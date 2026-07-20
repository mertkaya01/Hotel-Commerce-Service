import { FormBuilder, Validators } from '@angular/forms';
import { passwordsMatch } from './register';

/**
 * passwordsMatch form-level validator'i, uyusmazlik hatasini confirmPassword
 * KONTROLUNE yazmali (Material mat-error yalnizca kontrol hatasini gosterir).
 */
describe('passwordsMatch validator', () => {
  const fb = new FormBuilder();

  function group(pw: string, confirm: string) {
    return fb.group({
      password: [pw],
      confirmPassword: [confirm, [Validators.required]],
    });
  }

  it('sifreler farkliysa confirmPassword kontrolune passwordMismatch yazar', () => {
    const g = group('parola123', 'parola999');
    passwordsMatch(g);
    expect(g.get('confirmPassword')?.hasError('passwordMismatch')).toBe(true);
  });

  it('sifreler ayniysa hata birakmaz', () => {
    const g = group('parola123', 'parola123');
    passwordsMatch(g);
    expect(g.get('confirmPassword')?.hasError('passwordMismatch')).toBe(false);
  });

  it('kendi hatasini temizlerken required hatasini korur', () => {
    const g = group('parola123', '');
    g.get('confirmPassword')?.updateValueAndValidity();
    passwordsMatch(g);
    // bos confirm -> required hatasi durmali, mismatch olmamali
    expect(g.get('confirmPassword')?.hasError('required')).toBe(true);
    expect(g.get('confirmPassword')?.hasError('passwordMismatch')).toBe(false);
  });
});
