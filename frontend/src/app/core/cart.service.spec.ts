import 'fake-indexeddb/auto';
import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { CartService } from './cart.service';
import { DisplayCartItem, Product } from './models';

describe('CartService quantity changes', () => {
  let service: CartService;
  let originalOnline: PropertyDescriptor | undefined;

  const item: DisplayCartItem = {
    productId: 101,
    productName: 'Producto de prueba',
    unitPrice: 12.5,
    stock: 3,
    quantity: 1,
    totalPrice: 12.5,
  };

  beforeEach(() => {
    originalOnline = Object.getOwnPropertyDescriptor(navigator, 'onLine');
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false });
    localStorage.setItem('shopping_cart_user', `quantity-test-${Date.now()}-${Math.random()}`);
    TestBed.configureTestingModule({ providers: [provideHttpClient()] });
    service = TestBed.inject(CartService);
  });

  afterEach(() => {
    localStorage.clear();
    if (originalOnline) {
      Object.defineProperty(navigator, 'onLine', originalOnline);
    } else {
      delete (navigator as unknown as { onLine?: boolean }).onLine;
    }
  });

  it('keeps the selected quantity offline and removes the line at zero', async () => {
    await service.setQuantity(item, 3);
    expect(service.items()).toEqual([expect.objectContaining({ productId: 101, quantity: 3, totalPrice: 37.5 })]);

    await service.setQuantity(item, 2);
    expect(service.items()).toEqual([expect.objectContaining({ productId: 101, quantity: 2, totalPrice: 25 })]);

    await service.setQuantity(item, 0);
    expect(service.items()).toEqual([]);
    expect(service.message()).toBe('Eliminación guardada sin conexión');
  });

  it('does not add beyond the product stock', async () => {
    const product: Product = {
      id: item.productId,
      name: item.productName,
      price: item.unitPrice,
      stock: 1,
    };

    await service.add(product);
    await service.add(product);

    expect(service.items()).toEqual([expect.objectContaining({ productId: 101, quantity: 1 })]);
    expect(service.message()).toBe('No hay más unidades disponibles de este producto');
  });
});
