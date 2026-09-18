import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { UserApi } from './user-api';
import { UserResponse } from './user.model';

@Component({
  selector: 'app-user',
  imports: [ReactiveFormsModule],
  templateUrl: './user.html',
  styleUrl: './user.css',
})
export class User {
  private userApi = inject(UserApi);
  private fb = inject(FormBuilder);

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
        if (err.status === 400) {
          this.errorMessage.set('Invalid request');
        } else if (err.status === 409) {
          this.errorMessage.set('User already exists');
        } else {
          this.errorMessage.set('An unexpected error occurred');
        }
        this.saving.set(false);
      },
    });
  }
}