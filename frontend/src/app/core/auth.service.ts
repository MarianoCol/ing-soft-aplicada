import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, map, Observable, of, switchMap, tap } from 'rxjs';
import { Account, RegisterRequest } from './models';

interface LoginResponse {
  id_token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private static readonly TOKEN_KEY = 'shopping_cart_token';
  private static readonly USER_KEY = 'shopping_cart_user';
  private static readonly ACCOUNT_KEY = 'shopping_cart_account';

  readonly token = signal(localStorage.getItem(AuthService.TOKEN_KEY));
  readonly username = signal(localStorage.getItem(AuthService.USER_KEY));
  readonly account = signal<Account | null>(this.readStoredAccount());
  private readonly http = inject(HttpClient);

  login(username: string, password: string) {
    return this.http
      .post<LoginResponse>('/api/authenticate', { username, password, rememberMe: true })
      .pipe(
        tap(({ id_token }) => this.storeSession(username, id_token)),
        switchMap(() => this.loadAccount(true)),
      );
  }

  registerAndLogin(request: RegisterRequest): Observable<boolean> {
    return this.http.post<void>('/api/register', request).pipe(
      switchMap(() =>
        this.login(request.login, request.password).pipe(
          map(() => true),
          catchError(() => of(false)),
        ),
      ),
    );
  }

  storeSession(username: string, token: string): void {
    localStorage.setItem(AuthService.TOKEN_KEY, token);
    localStorage.setItem(AuthService.USER_KEY, username);
    this.token.set(token);
    this.username.set(username);
  }

  loadAccount(force = false): Observable<Account | null> {
    if (!this.token()) {
      return of(null);
    }
    if (!force && this.account()) {
      return of(this.account());
    }
    return this.http.get<Account>('/api/account').pipe(
      tap((account) => {
        localStorage.setItem(AuthService.ACCOUNT_KEY, JSON.stringify(account));
        localStorage.setItem(AuthService.USER_KEY, account.login);
        this.account.set(account);
        this.username.set(account.login);
      }),
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.logout();
          return of(null);
        }
        return of(this.account());
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(AuthService.TOKEN_KEY);
    localStorage.removeItem(AuthService.USER_KEY);
    localStorage.removeItem(AuthService.ACCOUNT_KEY);
    this.token.set(null);
    this.username.set(null);
    this.account.set(null);
  }

  isAuthenticated(): boolean {
    return Boolean(this.token());
  }

  isAdmin(): boolean {
    return this.account()?.authorities?.includes('ROLE_ADMIN') ?? false;
  }

  private readStoredAccount(): Account | null {
    const raw = localStorage.getItem(AuthService.ACCOUNT_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as Account;
    } catch {
      localStorage.removeItem(AuthService.ACCOUNT_KEY);
      return null;
    }
  }
}
