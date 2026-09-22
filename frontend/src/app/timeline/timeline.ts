import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
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
  protected submitting = signal(false);
  protected errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadTweets();
  }

  protected loadTweets(): void {
    this.tweetApi.getTweets().subscribe({
      next: (tweets) => this.tweets.set(tweets),
      error: (err) => this.errorMessage.set('Failed to load tweets'),
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
      error: (err) => {
        if (err.status === 400) {
          this.errorMessage.set('Invalid text');
        } else if (err.status === 404) {
          this.errorMessage.set('User not found');
        } else {
          this.errorMessage.set('Failed to create tweet');
        }
        this.submitting.set(false);
      },
    });
  }
}