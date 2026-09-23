import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { CreateTweetRequest, Page, TweetResponse } from './tweet.model';

@Service()
export class TweetApi {
  private http = inject(HttpClient);
  private readonly baseUrl = '/api/tweets';

  createTweet(request: CreateTweetRequest): Observable<TweetResponse> {
    return this.http.post<TweetResponse>(this.baseUrl, request);
  }

  getTweets(): Observable<TweetResponse[]> {
    return this.http
      .get<Page<TweetResponse>>(this.baseUrl)
      .pipe(map((page) => page.content));
  }
}