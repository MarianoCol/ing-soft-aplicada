import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.page').then((m) => m.LoginPage),
  },
  {
    path: 'products',
    loadComponent: () => import('./pages/products/products.page').then((m) => m.ProductsPage),
  },
  {
    path: 'cart',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/cart/cart.page').then((m) => m.CartPage),
  },
  {
    path: '',
    redirectTo: 'products',
    pathMatch: 'full',
  },
  { path: '**', redirectTo: 'products' },
];
