import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { UserApi } from './user-api';
import { UserResponse } from './user.model';
import { ApiError } from '../shared/api-error.model';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-user',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './user.html',
  styleUrl: './user.css',
})
export class User {
  private userApi = inject(UserApi);
  private fb = inject(FormBuilder);
  fieldErrors = signal<Record<string, string>>({});

  form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    displayName: ['', [Validators.required, Validators.maxLength(100)]],
  });

  createdUser = signal<UserResponse | null>(null);
  errorMessage = signal<string | null>(null);
  saving = signal(false);

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.createdUser.set(null);
    this.errorMessage.set(null);

    this.userApi.createUser(this.form.getRawValue()).subscribe({
      next: (user) => {
        this.createdUser.set(user);
        this.saving.set(false);
        this.form.reset();
      },
      error: (err: HttpErrorResponse) => {
        const apiError = err.error as ApiError | undefined;

        if (err.status === 409) {
          this.errorMessage.set(apiError?.message ?? 'User already exists');
        } else if (err.status === 400) {
          this.fieldErrors.set(apiError?.fieldErrors ?? {});
          this.errorMessage.set(apiError?.message ?? 'Invalid request');
        } else if (err.status === 0) {
          this.errorMessage.set('Backend not reachable');
        } else {
          this.errorMessage.set('An unexpected error occurred');
        }
        this.saving.set(false);
      },
    });
  }
}