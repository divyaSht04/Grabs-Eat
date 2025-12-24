import Cookies from 'js-cookie';
import { apiClient } from './api.service';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth.types';

const COOKIE_OPTIONS = {
  secure: import.meta.env.PROD, // Only use secure cookies in production
  sameSite: 'strict' as const,
  expires: 7, // 7 days
};

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    return apiClient.post<AuthResponse>('/auth/login', credentials);
  },

  async register(userData: RegisterRequest): Promise<AuthResponse> {
    return apiClient.post<AuthResponse>('/auth/register', userData);
  },

  async refreshToken(refreshToken: string): Promise<{ accessToken: string; tokenType: string }> {
    return apiClient.post('/auth/refresh', { refreshToken });
  },

  async logout(): Promise<void> {
    return apiClient.post('/auth/logout');
  },

  // Cookie-based storage helpers
  setTokens(accessToken: string, refreshToken: string): void {
    Cookies.set('accessToken', accessToken, {
      ...COOKIE_OPTIONS,
      expires: 1 / 24, // 1 hour for access token
    });
    Cookies.set('refreshToken', refreshToken, COOKIE_OPTIONS);
  },

  getAccessToken(): string | null {
    return Cookies.get('accessToken') || null;
  },

  getRefreshToken(): string | null {
    return Cookies.get('refreshToken') || null;
  },

  clearTokens(): void {
    Cookies.remove('accessToken');
    Cookies.remove('refreshToken');
    localStorage.removeItem('user');
  },
};
