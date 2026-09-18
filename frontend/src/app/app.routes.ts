import { Routes } from '@angular/router';
import { Hello } from './hello/hello';
import { User } from './user/user';
import { Profile } from './profile/profile';

export const routes: Routes = [
    { path: 'hello', component: Hello },
    { path: 'user', component: User },
    { path: 'users/:id', component: Profile }
];
