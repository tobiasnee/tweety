import { Routes } from '@angular/router';
import { Hello } from './hello/hello';
import { User } from './user/user';

export const routes: Routes = [
    { path: 'hello', component: Hello },
    { path: 'user', component: User }
];
