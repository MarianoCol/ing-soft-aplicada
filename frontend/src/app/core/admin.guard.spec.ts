import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, UrlTree } from '@angular/router';
import { firstValueFrom, isObservable, of } from 'rxjs';
import { adminGuard } from './admin.guard';
import { AuthService } from './auth.service';

describe('adminGuard', () => {
  const auth = {
    isAuthenticated: vi.fn(),
    isAdmin: vi.fn(),
    loadAccount: vi.fn(() => of(null)),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({ providers: [provideRouter([]), { provide: AuthService, useValue: auth }] });
  });

  it('redirects anonymous visitors to login', async () => {
    auth.isAuthenticated.mockReturnValue(false);
    const result = await runGuard();
    expect(TestBed.inject(Router).serializeUrl(result as UrlTree)).toBe('/login');
  });

  it('redirects authenticated non-admin users to products', async () => {
    auth.isAuthenticated.mockReturnValue(true);
    auth.isAdmin.mockReturnValue(false);
    const result = await runGuard();
    expect(TestBed.inject(Router).serializeUrl(result as UrlTree)).toBe('/products');
  });

  it('allows administrators', async () => {
    auth.isAuthenticated.mockReturnValue(true);
    auth.isAdmin.mockReturnValue(true);
    expect(await runGuard()).toBe(true);
  });

  async function runGuard() {
    const result = TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));
    return isObservable(result) ? firstValueFrom(result) : Promise.resolve(result);
  }
});
