import * as SecureStore from 'expo-secure-store';
import { apiService } from './api.service';
import { STORAGE_KEYS } from '@/src/constants/config';
import { 
  AuthResponse, 
  LoginRequest, 
  RegisterRequest, 
  User 
} from '@/src/types';

class AuthService {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await apiService.post<AuthResponse>('/auth/login', credentials);
    
    // Backend returns data directly, not wrapped in data property
    const authData = response.data || response as any;
    await this.storeAuthData(authData);
    
    return authData;
  }

  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await apiService.post<AuthResponse>('/auth/register', data);
    
    // Backend returns data directly, not wrapped in data property
    const authData = response.data || response as any;
    await this.storeAuthData(authData);
    
    return authData;
  }

  async logout(): Promise<void> {
    try {
      await apiService.post('/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      await this.clearAuthData();
    }
  }

  async getCurrentUser(): Promise<User | null> {
    try {
      const userDataString = await SecureStore.getItemAsync(STORAGE_KEYS.USER_DATA);
      if (userDataString) {
        return JSON.parse(userDataString);
      }
      return null;
    } catch (error) {
      console.error('Get current user error:', error);
      return null;
    }
  }

  async isAuthenticated(): Promise<boolean> {
    const token = await SecureStore.getItemAsync(STORAGE_KEYS.ACCESS_TOKEN);
    return !!token;
  }

  private async storeAuthData(authData: AuthResponse): Promise<void> {
    await SecureStore.setItemAsync(STORAGE_KEYS.ACCESS_TOKEN, authData.accessToken);
    await SecureStore.setItemAsync(STORAGE_KEYS.REFRESH_TOKEN, authData.refreshToken);
    await SecureStore.setItemAsync(STORAGE_KEYS.USER_DATA, JSON.stringify(authData.user));
  }

  private async clearAuthData(): Promise<void> {
    await SecureStore.deleteItemAsync(STORAGE_KEYS.ACCESS_TOKEN);
    await SecureStore.deleteItemAsync(STORAGE_KEYS.REFRESH_TOKEN);
    await SecureStore.deleteItemAsync(STORAGE_KEYS.USER_DATA);
  }
}

export const authService = new AuthService();
