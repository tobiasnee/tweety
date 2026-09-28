import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { TweetStore } from './tweet-store';
import { TweetApi } from './tweet-api';
import { LikeApi } from './like-api';
import { CurrentUser } from '../auth/current-user';
import { TweetResponse } from './tweet.model';
import { UserResponse } from '../user/user.model';

const me: UserResponse = {
  id: 1,
  username: 'max',
  email: 'max@mustermann.com',
  displayName: 'Mustermann',
  createdAt: '2026-01-01T10:00:00Z',
};

function tweet(id: number, text: string, likeCount = 0, likedByMe = false): TweetResponse {
  return {
    id,
    text,
    author: { id: 1, username: 'max', displayName: 'Mustermann' },
    createdAt: '2026-01-02T10:00:00Z',
    likeCount,
    likedByMe,
  };
}

describe('TweetStore', () => {
  let store: TweetStore;
  let tweetApi: {
    getTweets: ReturnType<typeof vi.fn>;
    createTweet: ReturnType<typeof vi.fn>;
    updateTweet: ReturnType<typeof vi.fn>;
    deleteTweet: ReturnType<typeof vi.fn>;
  };
  let likeApi: {
    like: ReturnType<typeof vi.fn>;
    unlike: ReturnType<typeof vi.fn>;
  };
  let currentUser: { user: ReturnType<typeof signal<UserResponse | null>> };

  beforeEach(() => {
    tweetApi = {
      getTweets: vi.fn().mockReturnValue(of([])),
      createTweet: vi.fn().mockReturnValue(of(tweet(3, 'Neu'))),
      updateTweet: vi.fn().mockReturnValue(of(tweet(1, 'Geändert'))),
      deleteTweet: vi.fn().mockReturnValue(of(undefined)),
    };

    likeApi = {
      like: vi.fn(),
      unlike: vi.fn(),
    };

    currentUser = { user: signal<UserResponse | null>(me) };

    TestBed.configureTestingModule({
      providers: [
        TweetStore,
        { provide: TweetApi, useValue: tweetApi },
        { provide: LikeApi, useValue: likeApi },
        { provide: CurrentUser, useValue: currentUser },
      ],
    });

    store = TestBed.inject(TweetStore);
  });


  it('loads tweets and resets loading', () => {
    tweetApi.getTweets.mockReturnValue(of([tweet(2, 'Zweiter'), tweet(1, 'Erster')]));

    store.load();

    expect(tweetApi.getTweets).toHaveBeenCalledWith(1);
    expect(store.tweets()).toHaveLength(2);
    expect(store.tweetCount()).toBe(2);
    expect(store.loading()).toBe(false);
  });


  it('replaces the tweets after creating one', () => {
    tweetApi.getTweets.mockReturnValue(of([tweet(3, 'Neu')]));

    store.create('Neu').subscribe();

    expect(tweetApi.createTweet).toHaveBeenCalledWith({ authorId: 1, text: 'Neu' });
    expect(store.tweets()).toHaveLength(1);
    expect(store.tweets()[0].text).toBe('Neu');
  });

  it('does not call the api when nobody is logged in', () => {
    currentUser.user.set(null);

    store.create('Neu').subscribe();

    expect(tweetApi.createTweet).not.toHaveBeenCalled();
  });


  it('updates one tweet and keeps its like state', () => {
    tweetApi.getTweets.mockReturnValue(of([tweet(1, 'Alt', 5, true), tweet(2, 'Anderer')]));
    store.load();

    tweetApi.updateTweet.mockReturnValue(of(tweet(1, 'Geändert', 0, false)));

    store.update(1, 'Geändert').subscribe();

    expect(store.tweets()[0].text).toBe('Geändert');
    expect(store.tweets()[0].likeCount).toBe(5);
    expect(store.tweets()[0].likedByMe).toBe(true);
    expect(store.tweets()[1].text).toBe('Anderer');
  });


  it('removes only the given tweet', () => {
    tweetApi.getTweets.mockReturnValue(of([tweet(1, 'Erster'), tweet(2, 'Zweiter')]));
    store.load();

    store.remove(1).subscribe();

    expect(tweetApi.deleteTweet).toHaveBeenCalledWith(1, 1);
    expect(store.tweets()).toHaveLength(1);
    expect(store.tweets()[0].id).toBe(2);
  });


  it('likes a tweet that is not liked yet', () => {
    const target = tweet(1, 'Erster', 0, false);
    tweetApi.getTweets.mockReturnValue(of([target]));
    store.load();

    likeApi.like.mockReturnValue(of({ tweetId: 1, likeCount: 1, likedByMe: true }));

    store.toggleLike(target).subscribe();

    expect(likeApi.like).toHaveBeenCalledWith(1, 1);
    expect(likeApi.unlike).not.toHaveBeenCalled();
    expect(store.tweets()[0].likeCount).toBe(1);
    expect(store.tweets()[0].likedByMe).toBe(true);
  });

  it('unlikes a tweet that is already liked', () => {
    const target = tweet(1, 'Erster', 1, true);
    tweetApi.getTweets.mockReturnValue(of([target]));
    store.load();

    likeApi.unlike.mockReturnValue(of({ tweetId: 1, likeCount: 0, likedByMe: false }));

    store.toggleLike(target).subscribe();

    expect(likeApi.unlike).toHaveBeenCalledWith(1, 1);
    expect(likeApi.like).not.toHaveBeenCalled();
    expect(store.tweets()[0].likeCount).toBe(0);
    expect(store.tweets()[0].likedByMe).toBe(false);
  });
});