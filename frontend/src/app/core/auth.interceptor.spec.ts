import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;

  beforeEach(() => {
    localStorage.setItem('shopping_cart_token', 'test-jwt');
    localStorage.setItem('shopping_cart_user', 'user');
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
    localStorage.clear();
  });

  it('adds the JWT only to relative API requests', () => {
    http.get('/api/products').subscribe();
    expect(controller.expectOne('/api/products').request.headers.get('Authorization')).toBe('Bearer test-jwt');

    http.get('https://example.test/public').subscribe();
    expect(controller.expectOne('https://example.test/public').request.headers.has('Authorization')).toBe(false);
  });
});
