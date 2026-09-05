import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map } from 'rxjs';
import {
  AdminDashboard,
  AdminOrderDetail,
  AdminOrderSummary,
  AdminProduct,
  AdminUser,
  OrderStatus,
  Product,
} from './models';

export interface PagedResult<T> {
  items: T[];
  total: number;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);

  dashboard() {
    return this.http.get<AdminDashboard>('/api/admin/dashboard');
  }

  products(page: number, search: string, active: '' | 'true' | 'false') {
    let params = new HttpParams().set('page', page).set('size', 10).set('sort', 'name,asc');
    if (search.trim()) params = params.set('search', search.trim());
    if (active) params = params.set('active', active);
    return this.toPage(this.http.get<AdminProduct[]>('/api/admin/products', { params, observe: 'response' }));
  }

  createProduct(product: Omit<Product, 'id'>) {
    return this.http.post<AdminProduct>('/api/admin/products', { ...product, active: true });
  }

  updateProduct(product: AdminProduct) {
    return this.http.put<AdminProduct>(`/api/admin/products/${product.id}`, product);
  }

  setProductActive(id: number, active: boolean) {
    return this.http.patch<AdminProduct>(`/api/admin/products/${id}/active`, { active });
  }

  deleteProduct(id: number) {
    return this.http.delete<void>(`/api/admin/products/${id}`);
  }

  orders(page: number, status: '' | OrderStatus) {
    let params = new HttpParams().set('page', page).set('size', 10).set('sort', 'placedDate,desc');
    if (status) params = params.set('status', status);
    return this.toPage(this.http.get<AdminOrderSummary[]>('/api/admin/orders', { params, observe: 'response' }));
  }

  order(id: number) {
    return this.http.get<AdminOrderDetail>(`/api/admin/orders/${id}`);
  }

  changeOrderStatus(id: number, status: Exclude<OrderStatus, 'PENDING'>) {
    return this.http.put<AdminOrderDetail>(`/api/admin/orders/${id}/status`, { status });
  }

  users(page: number) {
    const params = new HttpParams().set('page', page).set('size', 10).set('sort', 'login,asc');
    return this.toPage(this.http.get<AdminUser[]>('/api/admin/users', { params, observe: 'response' }));
  }

  updateUser(user: AdminUser) {
    return this.http.put<AdminUser>(`/api/admin/users/${user.login}`, user);
  }

  private toPage<T>(request: import('rxjs').Observable<HttpResponse<T[]>>) {
    return request.pipe(
      map((response) => ({ items: response.body ?? [], total: Number(response.headers.get('X-Total-Count') ?? 0) })),
    );
  }
}
