import { TestBed } from '@angular/core/testing';
import { TweetApi } from './tweet-api';

describe('TweetApi', () => {
  let service: TweetApi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TweetApi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
