import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { Login } from './login/login';
import { Timeline } from './timeline/timeline';
import { Profile } from './profile/profile';
import { User } from './user/user';

export const routes: Routes = [
  { path: '', redirectTo: 'timeline', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'user', component: User },
  { path: 'timeline', component: Timeline, canActivate: [authGuard] },
  { path: 'users/:id', component: Profile, canActivate: [authGuard] },
];