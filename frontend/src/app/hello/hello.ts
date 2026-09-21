import { Component, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AsyncPipe } from '@angular/common';

@Component({
  imports: [AsyncPipe],
  selector: 'app-hello',
  styleUrl: './hello.css',
  templateUrl: './hello.html',
})
export class Hello implements OnInit {
  private http = inject(HttpClient)
  message$!: Observable<string>;

  ngOnInit(): void {
    this.message$ = this.http.get('/api/hello', { responseType: 'text' });
  }
}
