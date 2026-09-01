import 'fake-indexeddb/auto';
import { TestBed } from '@angular/core/testing';
import { PendingCartItem } from './models';
import { OfflineCartStore } from './offline-cart.store';

describe('OfflineCartStore', () => {
  it('keeps pending quantities isolated by user until synchronization succeeds', async () => {
    const store = TestBed.inject(OfflineCartStore);
    const userItem: PendingCartItem = {
      id: 'user:101',
      username: 'user',
      productId: 101,
      productName: 'Producto offline',
      unitPrice: 12.5,
      stock: 8,
      quantity: 3,
      state: 'pending',
    };
    const otherItem: PendingCartItem = { ...userItem, id: 'admin:101', username: 'admin' };

    await store.put(userItem);
    await store.put(otherItem);

    expect(await store.list('user')).toEqual([userItem]);
    expect(await store.list('admin')).toEqual([otherItem]);

    await store.delete(userItem.id);
    await store.delete(otherItem.id);
    expect(await store.list('user')).toEqual([]);
  });
});
