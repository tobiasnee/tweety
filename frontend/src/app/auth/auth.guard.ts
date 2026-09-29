import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { CurrentUser } from './current-user';

/**
 * Führt Besucher ohne ausgewählten Benutzer zum Fake-Login.
 */

export const authGuard: CanActivateFn = () => {
  const currentUser = inject(CurrentUser);
  const router = inject(Router);

  return currentUser.isLoggedIn() ? true : router.createUrlTree(['/login']);
};