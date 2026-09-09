import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { OrderDetail, OrderSummary } from './models';
import { OrderService, PagedOrders } from './order.service';

describe('OrderService', () => {
  let service: OrderService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        OrderService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(OrderService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('loads a page of the current user orders', () => {
    const order: OrderSummary = {
      id: 42,
      placedDate: '2026-09-08T12:00:00Z',
      status: 'COMPLETED',
      totalPrice: 24,
    };

    let result: PagedOrders | undefined;

    service.orders(2).subscribe((response) => {
      result = response;
    });

    const request = http.expectOne(
      (candidate) => candidate.url === '/api/member/orders',
    );

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('10');
    expect(request.request.params.get('sort')).toBe('placedDate,desc');

    request.flush([order], {
      headers: {
        'X-Total-Count': '21',
      },
    });

    expect(result).toEqual({
      items: [order],
      total: 21,
    });
  });

  it('loads the detail of one current user order', () => {
    const detail: OrderDetail = {
      id: 42,
      placedDate: '2026-09-08T12:00:00Z',
      status: 'COMPLETED',
      totalPrice: 24,
      items: [
        {
          id: 10,
          quantity: 2,
          totalPrice: 24,
          productId: 5,
          productName: 'Teclado',
          unitPrice: 12,
        },
      ],
    };

    let result: OrderDetail | undefined;

    service.order(42).subscribe((response) => {
      result = response;
    });

    const request = http.expectOne('/api/member/orders/42');

    expect(request.request.method).toBe('GET');

    request.flush(detail);

    expect(result).toEqual(detail);
  });

  it('uses empty defaults when the paginated response has no data', () => {
    let result: PagedOrders | undefined;

    service.orders(0).subscribe((response) => {
      result = response;
    });

    const request = http.expectOne(
      (candidate) => candidate.url === '/api/member/orders',
    );

    request.flush(null);

    expect(result).toEqual({
      items: [],
      total: 0,
    });
  });
});