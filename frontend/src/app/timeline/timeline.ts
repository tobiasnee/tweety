import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { switchMap } from 'rxjs';
import { CurrentUser } from '../auth/current-user';
import { ConfigApi } from '../config/config-api';
import { LikeApi } from '../tweet/like-api';
import { TweetApi } from '../tweet/tweet-api';
import { TweetResponse } from '../tweet/tweet.model';
import { ApiError } from '../shared/api-error.model';

@Component({
  selector: 'app-timeline',
  imports: [DatePipe, RouterLink, ReactiveFormsModule],
  templateUrl: './timeline.html',
  styleUrl: './timeline.css',
})
export class Timeline implements OnInit {
  private tweetApi = inject(TweetApi);
  private likeApi = inject(LikeApi);
  private configApi = inject(ConfigApi);
  protected user = inject(CurrentUser).user;

  protected maxLength = signal(280);

  protected text = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.pattern(/\S/)],
  });

  protected editText = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.pattern(/\S/)],
  });

  private textValue = toSignal(this.text.valueChanges, { initialValue: '' });
  protected remaining = computed(() => this.maxLength() - this.textValue().length);

  protected tweets = signal<TweetResponse[]>([]);
  protected loading = signal(false);
  protected tweetCount = computed(() => this.tweets().length);

  protected editingId = signal<number | null>(null);
  protected submitting = signal(false);
  protected errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.applyLengthValidator(this.text);
    this.applyLengthValidator(this.editText);
    this.loadConfig();
    this.loadTweets();
  }

  private loadConfig(): void {
    this.configApi.getConfig().subscribe({
      next: (config) => {
        this.maxLength.set(config.maxTweetLength);
        this.applyLengthValidator(this.text);
        this.applyLengthValidator(this.editText);
      },
    });
  }

  private applyLengthValidator(control: FormControl<string>): void {
    control.setValidators([
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(this.maxLength()),
    ]);
    control.updateValueAndValidity();
  }

  protected loadTweets(): void {
    this.loading.set(true);
    this.tweetApi.getTweets(this.user()?.id).subscribe({
      next: (tweets) => {
        this.tweets.set(tweets);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected submit(): void {
    const me = this.user();
    if (!me || this.text.invalid) {
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.tweetApi
      .createTweet({ authorId: me.id, text: this.text.value })
      .pipe(switchMap(() => this.tweetApi.getTweets(me.id)))
      .subscribe({
        next: (tweets) => {
          this.tweets.set(tweets);
          this.text.reset();
          this.submitting.set(false);
        },
        error: (err: HttpErrorResponse) => {
          const apiError = err.error as ApiError | undefined;

          if (err.status === 400) {
            this.errorMessage.set(apiError?.message ?? 'Your tweet is not valid.');
          } else if (err.status === 404) {
            this.errorMessage.set(apiError?.message ?? 'Your user no longer exists. Please log in again.');
          } else {
            this.errorMessage.set('Could not post your tweet.');
          }
          this.submitting.set(false);
        },
      });
  }

  protected toggleLike(tweet: TweetResponse): void {
    const me = this.user();
    if (!me) {
      return;
    }

    this.errorMessage.set(null);

    const request = tweet.likedByMe
      ? this.likeApi.unlike(tweet.id, me.id)
      : this.likeApi.like(tweet.id, me.id);

    request.subscribe({
      next: (res) => {
        this.tweets.update((tweets) =>
          tweets.map((t) =>
            t.id === res.tweetId
              ? { ...t, likeCount: res.likeCount, likedByMe: res.likedByMe }
              : t
          )
        );
      },
      error: (err: HttpErrorResponse) => {
        const apiError = err.error as ApiError | undefined;
        this.errorMessage.set(apiError?.message ?? 'Could not update the like.');
      },
    });
  }

  protected startEdit(tweet: TweetResponse): void {
    this.editingId.set(tweet.id);
    this.editText.setValue(tweet.text);
    this.errorMessage.set(null);
  }

  protected cancelEdit(): void {
    this.editingId.set(null);
  }

  protected saveEdit(tweetId: number): void {
    const me = this.user();
    if (!me || this.editText.invalid) {
      return;
    }

    this.errorMessage.set(null);

    this.tweetApi.updateTweet(tweetId, { editorId: me.id, text: this.editText.value }).subscribe({
      next: (updated) => {
        this.tweets.update((tweets) =>
          tweets.map((tweet) =>
            tweet.id === updated.id
              ? { ...updated, likeCount: tweet.likeCount, likedByMe: tweet.likedByMe }
              : tweet
          )
        );
        this.editingId.set(null);
      },
      error: (err: HttpErrorResponse) => {
        const apiError = err.error as ApiError | undefined;

        if (err.status === 400 || err.status === 403 || err.status === 404) {
          this.errorMessage.set(apiError?.message ?? 'Could not update your tweet.');
        } else {
          this.errorMessage.set('Could not update your tweet.');
        }
      },
    });
  }

  protected deleteTweet(tweetId: number): void {
    const me = this.user();
    if (!me) {
      return;
    }

    this.errorMessage.set(null);

    this.tweetApi.deleteTweet(tweetId, me.id).subscribe({
      next: () => {
        this.tweets.update((tweets) => tweets.filter((tweet) => tweet.id !== tweetId));
        if (this.editingId() === tweetId) {
          this.editingId.set(null);
        }
      },
      error: (err: HttpErrorResponse) => {
        const apiError = err.error as ApiError | undefined;

        if (err.status === 403 || err.status === 404) {
          this.errorMessage.set(apiError?.message ?? 'Could not delete your tweet.');
        } else {
          this.errorMessage.set('Could not delete your tweet.');
        }
      },
    });
  }
}