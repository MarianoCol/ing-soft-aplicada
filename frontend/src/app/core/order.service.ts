import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { OrderDetail, OrderSummary } from './models';

export interface PagedOrders {
  items: OrderSummary[];
  total: number;
}

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);

  orders(page: number): Observable<PagedOrders> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', 10)
      .set('sort', 'placedDate,desc');

    return this.http
      .get<OrderSummary[]>('/api/member/orders', {
        params,
        observe: 'response',
      })
      .pipe(map((response) => this.toPagedOrders(response)));
  }

  order(id: number): Observable<OrderDetail> {
    return this.http.get<OrderDetail>(`/api/member/orders/${id}`);
  }

  private toPagedOrders(
    response: HttpResponse<OrderSummary[]>,
  ): PagedOrders {
    return {
      items: response.body ?? [],
      total: Number(response.headers.get('X-Total-Count') ?? 0),
    };
  }
}