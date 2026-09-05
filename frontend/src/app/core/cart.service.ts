import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';
import { CartView, DisplayCartItem, PendingCartItem, Product } from './models';
import { OfflineCartStore } from './offline-cart.store';

@Injectable({ providedIn: 'root' })
export class CartService {
  readonly cart = signal<CartView | null>(null);
  readonly pending = signal<PendingCartItem[]>([]);
  readonly message = signal<string | null>(null);
  readonly checkingOut = signal(false);

  readonly items = computed<DisplayCartItem[]>(() => {
    const merged = new Map<number, DisplayCartItem>();
    for (const item of this.cart()?.items ?? []) {
      merged.set(item.productId, { ...item });
    }
    for (const item of this.pending()) {
      if (item.quantity === 0) {
        merged.delete(item.productId);
        continue;
      }
      merged.set(item.productId, {
        productId: item.productId,
        productName: item.productName,
        unitPrice: item.unitPrice,
        stock: item.stock,
        quantity: item.quantity,
        totalPrice: item.unitPrice * item.quantity,
        syncState: item.state,
        error: item.error,
      });
    }
    return [...merged.values()];
  });

  readonly count = computed(() => this.items().reduce((sum, item) => sum + item.quantity, 0));
  readonly total = computed(() => this.items().reduce((sum, item) => sum + item.totalPrice, 0));

  private initialized = false;
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly store = inject(OfflineCartStore);

  constructor() {
    window.addEventListener('online', () => void this.synchronize());
  }

  async initialize(): Promise<void> {
    if (!this.auth.username()) return;
    await this.reloadPending();
    if (!this.initialized && navigator.onLine) {
      await this.refreshServerCart();
    }
    this.initialized = true;
    if (navigator.onLine) await this.synchronize();
  }

  async add(product: Product): Promise<void> {
    const username = this.auth.username();
    if (!username) return;
    const current = this.items().find((item) => item.productId === product.id)?.quantity ?? 0;
    const quantity = current + 1;
    const pending: PendingCartItem = {
      id: `${username}:${product.id}`,
      username,
      productId: product.id,
      productName: product.name,
      unitPrice: product.price,
      stock: product.stock,
      quantity,
      state: 'pending',
    };
    await this.store.put(pending);
    await this.reloadPending();
    this.message.set(navigator.onLine ? 'Sincronizando carrito…' : 'Guardado sin conexión');
    if (navigator.onLine) await this.synchronizeItem(pending);
  }

  async remove(item: DisplayCartItem): Promise<void> {
    const username = this.auth.username();
    if (!username) return;
    const pending: PendingCartItem = {
      id: `${username}:${item.productId}`,
      username,
      productId: item.productId,
      productName: item.productName,
      unitPrice: item.unitPrice,
      stock: item.stock,
      quantity: 0,
      state: 'pending',
    };
    await this.store.put(pending);
    await this.reloadPending();
    this.message.set(navigator.onLine ? 'Eliminando producto…' : 'Eliminación guardada sin conexión');
    if (navigator.onLine) await this.synchronizeItem(pending);
  }

  async checkout(): Promise<void> {
    if (!navigator.onLine) {
      this.message.set('Necesitás conexión para completar la compra');
      return;
    }
    this.checkingOut.set(true);
    try {
      await this.synchronize();
      if (this.pending().length > 0) {
        this.message.set('Resolvé los productos pendientes antes de comprar');
        return;
      }
      const order = await firstValueFrom(this.http.post<CartView>('/api/cart/checkout', {}));
      this.cart.set(null);
      await this.refreshServerCart();
      this.message.set(`Compra #${order.id} realizada correctamente`);
    } catch (error) {
      const response = error as HttpErrorResponse;
      this.message.set(
        response.status === 409
          ? 'No se pudo comprar: revisá el stock y los productos del carrito'
          : 'No se pudo completar la compra',
      );
    } finally {
      this.checkingOut.set(false);
    }
  }

  reset(): void {
    this.cart.set(null);
    this.pending.set([]);
    this.message.set(null);
    this.initialized = false;
  }

  async synchronize(): Promise<void> {
    if (!this.auth.isAuthenticated() || !navigator.onLine) return;
    for (const item of this.pending()) {
      await this.synchronizeItem(item);
    }
  }

  private async synchronizeItem(item: PendingCartItem): Promise<void> {
    try {
      const request =
        item.quantity === 0
          ? this.http.delete<CartView>(`/api/cart/items/${item.productId}`)
          : this.http.put<CartView>(`/api/cart/items/${item.productId}`, { quantity: item.quantity });
      const cart = await firstValueFrom(request);
      this.cart.set(cart);
      await this.store.delete(item.id);
      await this.reloadPending();
      this.message.set('Carrito sincronizado');
    } catch (error) {
      const response = error as HttpErrorResponse;
      const state = response.status === 401 ? 'auth' : response.status === 409 ? 'conflict' : 'pending';
      const updated = { ...item, state, error: this.errorMessage(response) } as PendingCartItem;
      await this.store.put(updated);
      await this.reloadPending();
      this.message.set(updated.error ?? 'No se pudo sincronizar');
    }
  }

  private async refreshServerCart(): Promise<void> {
    try {
      this.cart.set(await firstValueFrom(this.http.get<CartView>('/api/cart')));
    } catch {
      this.message.set('El carrito del servidor no está disponible');
    }
  }

  private async reloadPending(): Promise<void> {
    const username = this.auth.username();
    this.pending.set(username ? await this.store.list(username) : []);
  }

  private errorMessage(error: HttpErrorResponse): string {
    if (error.status === 401) return 'Volvé a iniciar sesión para sincronizar';
    if (error.status === 409) return 'El producto no está disponible o la cantidad supera el stock';
    return 'Pendiente de sincronización';
  }
}
