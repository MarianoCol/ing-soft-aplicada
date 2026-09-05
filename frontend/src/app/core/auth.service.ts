import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { tap } from 'rxjs';

interface LoginResponse {
  id_token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private static readonly TOKEN_KEY = 'shopping_cart_token';
  private static readonly USER_KEY = 'shopping_cart_user';

  readonly token = signal(localStorage.getItem(AuthService.TOKEN_KEY));
  readonly username = signal(localStorage.getItem(AuthService.USER_KEY));
  private readonly http = inject(HttpClient);

  login(username: string, password: string) {
    return this.http
      .post<LoginResponse>('/api/authenticate', { username, password, rememberMe: true })
      .pipe(tap(({ id_token }) => this.storeSession(username, id_token)));
  }

  storeSession(username: string, token: string): void {
    localStorage.setItem(AuthService.TOKEN_KEY, token);
    localStorage.setItem(AuthService.USER_KEY, username);
    this.token.set(token);
    this.username.set(username);
  }

  logout(): void {
    localStorage.removeItem(AuthService.TOKEN_KEY);
    localStorage.removeItem(AuthService.USER_KEY);
    this.token.set(null);
    this.username.set(null);
  }

  isAuthenticated(): boolean {
    return Boolean(this.token());
  }
}
