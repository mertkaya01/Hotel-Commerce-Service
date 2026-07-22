import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { Home } from './features/home/home';
import { HotelDetail } from './features/hotel-detail/hotel-detail';
import { Reservations } from './features/reservations/reservations';
import { Profile } from './features/profile/profile';
import { Favorites } from './features/favorites/favorites';
import { Host } from './features/host/host';
import { BecomeHost } from './features/become-host/become-host';
import { HostApplications } from './features/host-applications/host-applications';
import { AddHotel } from './features/add-hotel/add-hotel';
import { VerifyEmail } from './features/verify-email/verify-email';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { superAdminGuard } from './core/guards/super-admin.guard';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'hotels/:hotelCode', component: HotelDetail },
  { path: 'favorites', component: Favorites },
  { path: 'reservations', component: Reservations, canActivate: [authGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: 'become-host', component: BecomeHost, canActivate: [authGuard] },
  { path: 'host', component: Host, canActivate: [adminGuard] },
  { path: 'host/add-hotel', component: AddHotel, canActivate: [adminGuard] },
  { path: 'host/applications', component: HostApplications, canActivate: [superAdminGuard] },
  { path: 'host/hotel-listings', redirectTo: 'host/applications' },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'dogrula', component: VerifyEmail },
  { path: '**', redirectTo: '' },
];
