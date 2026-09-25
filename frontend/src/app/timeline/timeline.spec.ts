import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { Timeline } from './timeline';
import { TweetStore } from '../tweet/tweet-store';
import { ConfigApi } from '../config/config-api';
import { CurrentUser } from '../auth/current-user';
import { UserResponse } from '../user/user.model';

const me: UserResponse = {
  id: 1,
  username: 'max',
  email: 'max@mustermann.com',
  displayName: 'Mustermann',
  createdAt: '2026-01-01T10:00:00Z',
};

describe('Timeline', () => {
  let fixture: ComponentFixture<Timeline>;
  let component: Timeline;
  let store: {
    tweets: ReturnType<typeof signal<never[]>>;
    loading: ReturnType<typeof signal<boolean>>;
    tweetCount: ReturnType<typeof signal<number>>;
    load: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    store = {
      tweets: signal([]),
      loading: signal(false),
      tweetCount: signal(0),
      load: vi.fn(),
      create: vi.fn().mockReturnValue(of([])),
    };

    await TestBed.configureTestingModule({
      imports: [Timeline],
      providers: [
        provideRouter([]),
        { provide: TweetStore, useValue: store },
        { provide: ConfigApi, useValue: { getConfig: () => of({ maxTweetLength: 280 }) } },
        { provide: CurrentUser, useValue: { user: signal(me) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Timeline);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('does not submit when the text is empty', () => {
    component['text'].setValue('');

    component['submit']();

    expect(store.create).not.toHaveBeenCalled();
  });

  it('does not submit when the text is only whitespace', () => {
    component['text'].setValue('   ');

    component['submit']();

    expect(store.create).not.toHaveBeenCalled();
  });

  it('submits a valid text and clears the field', () => {
    component['text'].setValue('Mein erster Tweet');

    component['submit']();

    expect(store.create).toHaveBeenCalledWith('Mein erster Tweet');
    expect(component['text'].value).toBe('');
    expect(component['submitting']()).toBe(false);
  });

  it('shows the server message when submitting fails', () => {
    store.create.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 400,
        error: { status: 400, message: 'Text ist zu lang.', fieldErrors: {} },
      }))
    );
    component['text'].setValue('Zu langer Text');

    component['submit']();

    expect(component['errorMessage']()).toBe('Text ist zu lang.');
    expect(component['submitting']()).toBe(false);
  });
});