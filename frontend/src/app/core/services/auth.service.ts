import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, AuthUser, LoginRequest, RegisterRequest } from '../models/auth.model';

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  private readonly currentUserSignal = signal<AuthUser | null>(this.loadUserFromStorage());
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isLoggedIn = computed(() => this.currentUserSignal() !== null);

  constructor(private http: HttpClient) {}

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/register`, request)
      .pipe(tap((response) => this.setSession(response)));
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/login`, request)
      .pipe(tap((response) => this.setSession(response)));
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSignal.set(null);
  }

  /** Maildeki linkten gelen token'ı doğrular (herkese açık). */
  verifyEmail(token: string): Observable<{ message: string }> {
    const params = new HttpParams().set('token', token);
    return this.http.post<{ message: string }>(`${this.apiUrl}/verify`, null, { params }).pipe(
      tap(() => this.markVerifiedInSession()),
    );
  }

  /** Doğrulama mailini yeniden gönder (giriş yapmış kullanıcı). */
  resendVerification(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${environment.apiUrl}/users/me/resend-verification`,
      null,
    );
  }

  /** Oturumdaki kullanıcıyı "doğrulandı" olarak işaretle (rozet tazelensin). */
  markVerifiedInSession(): void {
    const current = this.currentUserSignal();
    if (!current || current.emailVerified) return;
    const updated: AuthUser = { ...current, emailVerified: true };
    localStorage.setItem(USER_KEY, JSON.stringify(updated));
    this.currentUserSignal.set(updated);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  // profil guncellemesinden sonra navbar'daki ismi tazelemek icin
  updateCurrentUserName(firstName: string, lastName: string): void {
    const current = this.currentUserSignal();
    if (!current) return;
    const updated: AuthUser = { ...current, firstName, lastName };
    localStorage.setItem(USER_KEY, JSON.stringify(updated));
    this.currentUserSignal.set(updated);
  }

  // ev sahibi (ADMIN) olunca rolu UI'da tazelemek icin
  updateCurrentUserRole(role: string): void {
    const current = this.currentUserSignal();
    if (!current) return;
    const updated: AuthUser = { ...current, role };
    localStorage.setItem(USER_KEY, JSON.stringify(updated));
    this.currentUserSignal.set(updated);
  }

  // ev sahibi (property owner) = ADMIN
  readonly isHost = computed(() => this.currentUserSignal()?.role === 'ADMIN');
  // platform yöneticisi (başvuruları değerlendirir) = SUPER_ADMIN
  readonly isSuperAdmin = computed(() => this.currentUserSignal()?.role === 'SUPER_ADMIN');

  private setSession(response: AuthResponse): void {
    const user: AuthUser = {
      email: response.email,
      firstName: response.firstName,
      lastName: response.lastName,
      role: response.role,
      emailVerified: response.emailVerified,
    };
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.currentUserSignal.set(user);
  }

  private loadUserFromStorage(): AuthUser | null {
    const stored = localStorage.getItem(USER_KEY);
    return stored ? (JSON.parse(stored) as AuthUser) : null;
  }
}
