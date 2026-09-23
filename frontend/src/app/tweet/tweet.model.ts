export interface CreateTweetRequest {
  authorId: number;
  text: string;
}

export interface TweetAuthor {
  id: number;
  username: string;
  displayName: string;
}

export interface TweetResponse {
  id: number;
  text: string;
  author: TweetAuthor;
  createdAt: string;
  likeCount: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  last: boolean;
}

export interface UpdateTweetRequest {
  editorId: number;
  text: string;
}