import { Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CurrentUser } from '../auth/current-user';

@Component({
  selector: 'app-timeline',
  imports: [DatePipe, RouterLink],
  templateUrl: './timeline.html',
  styleUrl: './timeline.css',
})
export class Timeline {
  protected user = inject(CurrentUser).user;
}