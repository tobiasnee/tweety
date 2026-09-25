import { TestBed } from '@angular/core/testing';
import { LikeApi } from './like-api';

describe('LikeApi', () => {
  let service: LikeApi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LikeApi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
