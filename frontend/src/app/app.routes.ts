import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { adminGuard } from './core/admin.guard';

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
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin/admin-layout.component').then((m) => m.AdminLayoutComponent),
    children: [
      { path: '', loadComponent: () => import('./pages/admin/dashboard.page').then((m) => m.AdminDashboardPage) },
      { path: 'products', loadComponent: () => import('./pages/admin/admin-products.page').then((m) => m.AdminProductsPage) },
      { path: 'orders', loadComponent: () => import('./pages/admin/admin-orders.page').then((m) => m.AdminOrdersPage) },
      { path: 'users', loadComponent: () => import('./pages/admin/admin-users.page').then((m) => m.AdminUsersPage) },
    ],
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
