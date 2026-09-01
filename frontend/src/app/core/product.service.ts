import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Product } from './models';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);

  list() {
    const params = new HttpParams().set('page', 0).set('size', 50).set('sort', 'name,asc');
    return this.http.get<Product[]>('/api/products', { params });
  }
}
