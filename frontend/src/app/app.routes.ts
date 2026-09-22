import { Routes } from '@angular/router';
import { Hello } from './hello/hello';
import { User } from './user/user';
import { Profile } from './profile/profile';
import { Login } from './login/login';
import { Timeline } from './timeline/timeline';

export const routes: Routes = [
    { path: '', redirectTo: 'login', pathMatch: 'full' },
    { path: 'login', component: Login },
    { path: 'timeline', component: Timeline },
    { path: 'hello', component: Hello },
    { path: 'user', component: User },
    { path: 'users/:id', component: Profile }
];
