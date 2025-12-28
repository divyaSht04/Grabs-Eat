export interface User {
  id: number;
  fullName: string;
  email: string;
  phoneNumber?: string;
  role: UserRole;
  isActive: boolean;
  isEmailVerified: boolean;
  createdAt: string;
}

export enum UserRole {
  CUSTOMER = 'CUSTOMER',
  STAFF = 'STAFF',
  OWNER = 'OWNER',
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  phoneNumber?: string;
  role: UserRole;
}

export interface ApiError {
  message: string;
  status: number;
  errors?: Record<string, string[]>;
}

export interface ApiResponse<T = any> {
  data?: T;
  message?: string;
  success: boolean;
}
