import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
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

  private textValue = toSignal(this.text.valueChanges, { initialValue: '' });
  protected remaining = computed(() => this.maxLength - this.textValue().length);

  protected tweets = signal<TweetResponse[]>([]);
  protected loading = signal(false);
  protected tweetCount = computed(() => this.tweets().length);

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

    this.tweetApi.createTweet({ authorId: me.id, text: this.text.value }).subscribe({
      next: () => {
        this.text.reset();
        this.submitting.set(false);
        this.loadTweets();
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
}