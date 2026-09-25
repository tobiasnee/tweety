import { computed, effect, Service, signal } from '@angular/core';
import { UserResponse } from '../user/user.model';

const STORAGE_KEY = 'tweety.currentUser';

@Service()
export class CurrentUser {
  private readonly _user = signal<UserResponse | null>(this.loadFromStorage());

  readonly user = this._user.asReadonly();
  readonly isLoggedIn = computed(() => this._user() !== null);

  constructor() {
    effect(() => {
      const user = this._user();
      try {
        if (user) {
          localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
        } else {
          localStorage.removeItem(STORAGE_KEY);
        }
      } catch {
      }
    });
  }

  login(user: UserResponse): void {
    this._user.set(user);
  }

  logout(): void {
    this._user.set(null);
  }

  private loadFromStorage(): UserResponse | null {
    const item = localStorage.getItem(STORAGE_KEY);
    if (!item) {
      return null;
    }
    try {
      return JSON.parse(item) as UserResponse;
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }
}