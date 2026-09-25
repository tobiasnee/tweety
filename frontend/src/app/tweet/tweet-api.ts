import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { CreateTweetRequest, Page, TweetResponse, UpdateTweetRequest } from './tweet.model';

@Service()
export class TweetApi {
  private http = inject(HttpClient);
  private readonly baseUrl = '/api/tweets';

  createTweet(request: CreateTweetRequest): Observable<TweetResponse> {
    return this.http.post<TweetResponse>(this.baseUrl, request);
  }

getTweets(currentUserId?: number): Observable<TweetResponse[]> {
  const options = currentUserId !== undefined
    ? { params: { currentUserId } }
    : {};

  return this.http
    .get<Page<TweetResponse>>(this.baseUrl, options)
    .pipe(map((page) => page.content));
}

    updateTweet(id: number, request: UpdateTweetRequest): Observable<TweetResponse> {
    return this.http.put<TweetResponse>(`${this.baseUrl}/${id}`, request);
  }

  deleteTweet(id: number, editorId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, {
      params: { editorId },
    });
  }
}