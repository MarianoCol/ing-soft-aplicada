import { Injectable } from '@angular/core';
import { DBSchema, IDBPDatabase, openDB } from 'idb';
import { PendingCartItem } from './models';

interface ShoppingCartDb extends DBSchema {
  pendingItems: {
    key: string;
    value: PendingCartItem;
    indexes: { username: string };
  };
}

@Injectable({ providedIn: 'root' })
export class OfflineCartStore {
  private readonly database: Promise<IDBPDatabase<ShoppingCartDb>> = openDB<ShoppingCartDb>('shopping-cart-offline', 1, {
    upgrade(db) {
      const store = db.createObjectStore('pendingItems', { keyPath: 'id' });
      store.createIndex('username', 'username');
    },
  });

  async list(username: string): Promise<PendingCartItem[]> {
    return (await this.database).getAllFromIndex('pendingItems', 'username', username);
  }

  async put(item: PendingCartItem): Promise<void> {
    await (await this.database).put('pendingItems', item);
  }

  async delete(id: string): Promise<void> {
    await (await this.database).delete('pendingItems', id);
  }
}
