import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { OrderDetail, OrderSummary } from '../../core/models';
import { OrderService } from '../../core/order.service';
import { OrdersPage } from './orders.page';

describe('OrdersPage', () => {
  const summary: OrderSummary = {
    id: 42,
    placedDate: '2026-09-08T12:00:00Z',
    status: 'COMPLETED',
    totalPrice: 24,
  };

  const detail: OrderDetail = {
    ...summary,
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

  let fixture: ComponentFixture<OrdersPage>;
  let component: OrdersPage;
  let ordersMock: ReturnType<typeof vi.fn>;
  let orderMock: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    ordersMock = vi.fn().mockReturnValue(
      of({
        items: [summary],
        total: 1,
      }),
    );

    orderMock = vi.fn().mockReturnValue(of(detail));

    TestBed.configureTestingModule({
      imports: [OrdersPage],
      providers: [
        provideRouter([]),
        {
          provide: OrderService,
          useValue: {
            orders: ordersMock,
            order: orderMock,
          },
        },
      ],
    });

    fixture = TestBed.createComponent(OrdersPage);
    component = fixture.componentInstance;
  });

  it('loads the first page on initialization', () => {
    fixture.detectChanges();

    expect(ordersMock).toHaveBeenCalledWith(0);
    expect(component.orders()).toEqual([summary]);
    expect(component.total()).toBe(1);
    expect(component.loading()).toBe(false);
    expect(component.error()).toBe('');
  });

  it('loads an order detail when the user opens it', () => {
    fixture.detectChanges();

    component.open(summary);

    expect(orderMock).toHaveBeenCalledWith(42);
    expect(component.selected()).toEqual(detail);
  });

  it('shows an error when the history cannot be loaded', () => {
    ordersMock.mockReturnValue(
      throwError(() => new Error('network error')),
    );

    fixture.detectChanges();

    expect(component.orders()).toEqual([]);
    expect(component.loading()).toBe(false);
    expect(component.error()).toContain('No se pudo cargar');
  });

  it('loads the next page when more results exist', () => {
    fixture.detectChanges();
    component.total.set(21);

    component.next();

    expect(component.page).toBe(1);
    expect(ordersMock).toHaveBeenLastCalledWith(1);
    expect(component.selected()).toBeNull();
  });
});