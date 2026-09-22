import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CurrentUser } from './auth/current-user';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private router = inject(Router);
  protected currentUser = inject(CurrentUser);

  logout(): void {
    this.currentUser.logout();
    this.router.navigate(['/login']);
  }
}