import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppConfig } from './config.model';

@Service()
export class ConfigApi {
  private http = inject(HttpClient);

  getConfig(): Observable<AppConfig> {
    return this.http.get<AppConfig>('/api/config');
  }
}