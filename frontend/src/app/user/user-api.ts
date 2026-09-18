import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateUserRequest, UserResponse } from './user.model';

@Injectable({ providedIn: 'root' })
export class UserApi {
  private http = inject(HttpClient);

  createUser(request: CreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>('/api/users', request);
  }
}