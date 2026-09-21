export interface CreateUserRequest {
  username: string;
  email: string;
  displayName: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  displayName: string;
}