import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { CurrentUser } from '../auth/current-user';
import { ConfigApi } from '../config/config-api';
import { TweetStore } from '../tweet/tweet-store';
import { TweetResponse } from '../tweet/tweet.model';
import { ApiError } from '../shared/api-error.model';

@Component({
  selector: 'app-timeline',
  imports: [DatePipe, RouterLink, ReactiveFormsModule],
  templateUrl: './timeline.html',
  styleUrl: './timeline.css',
})
export class Timeline implements OnInit {
  private configApi = inject(ConfigApi);
  protected store = inject(TweetStore);
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

  protected editingId = signal<number | null>(null);
  protected submitting = signal(false);
  protected errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.applyLengthValidator(this.text);
    this.applyLengthValidator(this.editText);
    this.loadConfig();
    this.store.load();
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

  protected submit(): void {
    if (this.text.invalid) {
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.store.create(this.text.value).subscribe({
      next: () => {
        this.text.reset();
        this.submitting.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.showError(err, 'Could not post your tweet.');
        this.submitting.set(false);
      },
    });
  }

  protected toggleLike(tweet: TweetResponse): void {
    this.errorMessage.set(null);

    this.store.toggleLike(tweet).subscribe({
      error: (err: HttpErrorResponse) => this.showError(err, 'Could not update the like.'),
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
    if (this.editText.invalid) {
      return;
    }

    this.errorMessage.set(null);

    this.store.update(tweetId, this.editText.value).subscribe({
      next: () => this.editingId.set(null),
      error: (err: HttpErrorResponse) => this.showError(err, 'Could not update your tweet.'),
    });
  }

  protected deleteTweet(tweetId: number): void {
    this.errorMessage.set(null);

    this.store.remove(tweetId).subscribe({
      next: () => {
        if (this.editingId() === tweetId) {
          this.editingId.set(null);
        }
      },
      error: (err: HttpErrorResponse) => this.showError(err, 'Could not delete your tweet.'),
    });
  }

  private showError(err: HttpErrorResponse, fallback: string): void {
    const apiError = err.error as ApiError | undefined;
    this.errorMessage.set(apiError?.message ?? fallback);
  }
}