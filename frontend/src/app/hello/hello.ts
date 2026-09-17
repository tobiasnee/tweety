import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  imports: [],
  selector: 'app-hello',
  styleUrl: './hello.css',
  templateUrl: './hello.html',
})
export class Hello {
  private http = inject(HttpClient);

  message = signal<string | null>(null);
  error = signal<string | null>(null);

  constructor() {
    this.http.get('/api/hello', { responseType: 'text' }).subscribe({
      next: (text) => this.message.set(text),
      error: () => this.error.set('Fehler beim Erreichen der API'),
    });
  }
}
