import { TestBed } from '@angular/core/testing';
import { ConfigApi } from './config-api';

describe('ConfigApi', () => {
  let service: ConfigApi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConfigApi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
