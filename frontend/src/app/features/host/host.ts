import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-host',
  imports: [RouterLink, MatIconModule],
  templateUrl: './host.html',
  styleUrl: './host.scss',
})
export class Host {
  private readonly authService = inject(AuthService);
  readonly user = this.authService.currentUser;
}
