import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [AuthService, provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => { http.verify(); localStorage.clear(); });

  it('loads and persists the administrator identity after login', () => {
    service.login('admin', 'admin').subscribe();
    http.expectOne('/api/authenticate').flush({ id_token: 'jwt' });
    http.expectOne('/api/account').flush({
      id: 1, login: 'admin', activated: true, authorities: ['ROLE_USER', 'ROLE_ADMIN'],
    });

    expect(service.isAdmin()).toBe(true);
    expect(localStorage.getItem('shopping_cart_account')).toContain('ROLE_ADMIN');
  });

  it('clears an expired session when account loading returns 401', () => {
    service.storeSession('admin', 'expired');
    service.loadAccount(true).subscribe();
    http.expectOne('/api/account').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(service.isAuthenticated()).toBe(false);
    expect(service.account()).toBeNull();
  });
});
