import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { switchMap } from 'rxjs';
import { CurrentUser } from '../auth/current-user';
import { TweetApi } from '../tweet/tweet-api';
import { TweetResponse } from '../tweet/tweet.model';

@Component({
  selector: 'app-timeline',
  imports: [DatePipe, RouterLink, ReactiveFormsModule],
  templateUrl: './timeline.html',
  styleUrl: './timeline.css',
})
export class Timeline implements OnInit {
  private tweetApi = inject(TweetApi);
  protected user = inject(CurrentUser).user;

  protected readonly maxLength = 280;

  protected text = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(this.maxLength), Validators.pattern(/\S/)],
  });

  protected editText = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(this.maxLength), Validators.pattern(/\S/)],
  });

  private textValue = toSignal(this.text.valueChanges, { initialValue: '' });
  protected remaining = computed(() => this.maxLength - this.textValue().length);

  protected tweets = signal<TweetResponse[]>([]);
  protected loading = signal(false);
  protected tweetCount = computed(() => this.tweets().length);

  protected editingId = signal<number | null>(null);
  protected submitting = signal(false);
  protected errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadTweets();
  }

  protected loadTweets(): void {
    this.loading.set(true);
    this.tweetApi.getTweets().subscribe({
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
      .pipe(switchMap(() => this.tweetApi.getTweets()))
      .subscribe({
        next: (tweets) => {
          this.tweets.set(tweets);
          this.text.reset();
          this.submitting.set(false);
        },
        error: (err: HttpErrorResponse) => {
          if (err.status === 400) {
            this.errorMessage.set('Your tweet is not valid.');
          } else if (err.status === 404) {
            this.errorMessage.set('Your user no longer exists. Please log in again.');
          } else {
            this.errorMessage.set('Could not post your tweet.');
          }
          this.submitting.set(false);
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
          tweets.map((tweet) => (tweet.id === updated.id ? updated : tweet))
        );
        this.editingId.set(null);
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 404) {
          this.errorMessage.set('This tweet no longer exists.');
        } else if (err.status === 403) {
          this.errorMessage.set('You can only edit your own tweets.');
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
        if (err.status === 404) {
          this.errorMessage.set('This tweet no longer exists.');
        } else if (err.status === 403) {
          this.errorMessage.set('You can only delete your own tweets.');
        } else {
          this.errorMessage.set('Could not delete your tweet.');
        }
      },
    });
  }
}