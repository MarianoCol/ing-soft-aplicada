import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { CartService } from './cart.service';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly auth = inject(AuthService);
  private readonly cart = inject(CartService);
  private readonly router = inject(Router);

  logout(): void {
    this.auth.logout();
    this.cart.reset();
    // Go through the root redirect so Productos reloads its public catalog even
    // when the user logs out while already on that route.
    void this.router.navigateByUrl('/', { replaceUrl: true });
  }
}
