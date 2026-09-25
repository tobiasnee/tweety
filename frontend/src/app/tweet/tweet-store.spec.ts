import { TestBed } from '@angular/core/testing';
import { TweetStore } from './tweet-store';

describe('TweetStore', () => {
  let service: TweetStore;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TweetStore);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
