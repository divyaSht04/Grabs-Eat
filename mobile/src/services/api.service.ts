import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import * as SecureStore from 'expo-secure-store';
import { API_CONFIG, STORAGE_KEYS } from '@/src/constants/config';
import { ApiError, ApiResponse } from '@/src/types';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_CONFIG.BASE_URL,
      timeout: API_CONFIG.TIMEOUT,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    this.api.interceptors.request.use(
      async (config: InternalAxiosRequestConfig) => {
        const token = await SecureStore.getItemAsync(STORAGE_KEYS.ACCESS_TOKEN);
        if (token && config.headers) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    this.api.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        if (error.response?.status === 401) {
          await this.clearTokens();
        }
        return Promise.reject(this.handleError(error));
      }
    );
  }

  private handleError(error: AxiosError): ApiError {
    if (error.response) {
      return {
        message: (error.response.data as any)?.message || 'An error occurred',
        status: error.response.status,
        errors: (error.response.data as any)?.errors,
      };
    }
    return {
      message: error.message || 'Network error',
      status: 0,
    };
  }

  private async clearTokens() {
    await SecureStore.deleteItemAsync(STORAGE_KEYS.ACCESS_TOKEN);
    await SecureStore.deleteItemAsync(STORAGE_KEYS.REFRESH_TOKEN);
    await SecureStore.deleteItemAsync(STORAGE_KEYS.USER_DATA);
  }

  public get<T = any>(url: string, config = {}): Promise<ApiResponse<T>> {
    return this.api.get(url, config).then((res) => res.data);
  }

  public post<T = any>(url: string, data?: any, config = {}): Promise<ApiResponse<T>> {
    return this.api.post(url, data, config).then((res) => res.data);
  }

  public put<T = any>(url: string, data?: any, config = {}): Promise<ApiResponse<T>> {
    return this.api.put(url, data, config).then((res) => res.data);
  }

  public delete<T = any>(url: string, config = {}): Promise<ApiResponse<T>> {
    return this.api.delete(url, config).then((res) => res.data);
  }
}

export const apiService = new ApiService();
