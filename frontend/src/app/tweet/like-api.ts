import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LikeResponse } from './tweet.model';

@Service()
export class LikeApi {
  private http = inject(HttpClient);

  like(tweetId: number, userId: number): Observable<LikeResponse> {
    return this.http.post<LikeResponse>(`/api/tweets/${tweetId}/likes`, null, {
      params: { userId },
    });
  }

  unlike(tweetId: number, userId: number): Observable<LikeResponse> {
    return this.http.delete<LikeResponse>(`/api/tweets/${tweetId}/likes`, {
      params: { userId },
    });
  }
}