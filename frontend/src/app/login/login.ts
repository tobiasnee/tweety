import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { httpResource } from '@angular/common/http';
import { CurrentUser } from '../auth/current-user';
import { UserResponse } from '../user/user.model';

@Component({
  selector: 'app-login',
  imports: [RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private currentUser = inject(CurrentUser);
  private router = inject(Router);

  users = httpResource<UserResponse[]>(() => '/api/users', { defaultValue: [] });

  select(user: UserResponse): void {
    this.currentUser.login(user);
    this.router.navigate(['/timeline']);
  }
}