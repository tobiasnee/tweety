import { Component, computed, input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { httpResource, HttpErrorResponse } from '@angular/common/http';
import { UserResponse } from '../user/user.model';

@Component({
  selector: 'app-profile',
  imports: [DatePipe],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {
  id = input.required<string>();

  user = httpResource<UserResponse>(() => `/api/users/${this.id()}`);

  errorMessage = computed(() => {
    const error = this.user.error() as HttpErrorResponse | undefined;
    if (!error) {
      return null;
    }
    if (error.status === 404) {
      return `No user found with ID ${this.id()}.`;
    }
    if (error.status === 0) {
      return 'Backend not reachable.';
    }
    return 'An unexpected error occurred.';
  });
}