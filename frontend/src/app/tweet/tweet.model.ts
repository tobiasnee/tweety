import { UserResponse } from '../user/user.model';

export interface CreateTweetRequest {
  authorId: number;
  text: string;
}

export interface TweetResponse {
  id: number;
  text: string;
  author: UserResponse;
  createdAt: string;
}