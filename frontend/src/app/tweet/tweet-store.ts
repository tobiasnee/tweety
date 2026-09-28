import { computed, inject, Service, signal } from '@angular/core';
import { EMPTY, Observable, switchMap, tap } from 'rxjs';
import { CurrentUser } from '../auth/current-user';
import { LikeApi } from './like-api';
import { TweetApi } from './tweet-api';
import { TweetResponse } from './tweet.model';

@Service()
export class TweetStore {
  private tweetApi = inject(TweetApi);
  private likeApi = inject(LikeApi);
  private currentUser = inject(CurrentUser);

  private readonly _tweets = signal<TweetResponse[]>([]);
  private readonly _loading = signal(false);

  readonly tweets = this._tweets.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly tweetCount = computed(() => this._tweets().length);

  load(): void {
    this._loading.set(true);
    this.tweetApi.getTweets(this.currentUser.user()?.id).subscribe({
      next: (tweets) => {
        this._tweets.set(tweets);
        this._loading.set(false);
      },
      error: () => this._loading.set(false),
    });
  }

  create(text: string): Observable<TweetResponse[]> {
    const me = this.currentUser.user();
    if (!me) {
      return EMPTY;
    }

    return this.tweetApi.createTweet({ authorId: me.id, text }).pipe(
      switchMap(() => this.tweetApi.getTweets(me.id)),
      tap((tweets) => this._tweets.set(tweets))
    );
  }

  update(tweetId: number, text: string): Observable<TweetResponse> {
    const me = this.currentUser.user();
    if (!me) {
      return EMPTY;
    }

    return this.tweetApi.updateTweet(tweetId, { editorId: me.id, text }).pipe(
      tap((updated) =>
        this._tweets.update((tweets) =>
          tweets.map((tweet) =>
            tweet.id === updated.id
              ? { ...updated, likeCount: tweet.likeCount, likedByMe: tweet.likedByMe }
              : tweet
          )
        )
      )
    );
  }

  remove(tweetId: number): Observable<void> {
    const me = this.currentUser.user();
    if (!me) {
      return EMPTY;
    }

    return this.tweetApi.deleteTweet(tweetId, me.id).pipe(
      tap(() =>
        this._tweets.update((tweets) => tweets.filter((tweet) => tweet.id !== tweetId))
      )
    );
  }

  toggleLike(tweet: TweetResponse): Observable<unknown> {
    const me = this.currentUser.user();
    if (!me) {
      return EMPTY;
    }

    const request = tweet.likedByMe
      ? this.likeApi.unlike(tweet.id, me.id)
      : this.likeApi.like(tweet.id, me.id);

    return request.pipe(
      tap((res) =>
        this._tweets.update((tweets) =>
          tweets.map((t) =>
            t.id === res.tweetId
              ? { ...t, likeCount: res.likeCount, likedByMe: res.likedByMe }
              : t
          )
        )
      )
    );
  }
}