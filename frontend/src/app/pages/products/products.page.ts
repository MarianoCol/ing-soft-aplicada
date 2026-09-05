import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import {
  IonBadge,
  IonButton,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardSubtitle,
  IonCardTitle,
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { CartService } from '../../core/cart.service';
import { AuthService } from '../../core/auth.service';
import { CatalogProduct, Product } from '../../core/models';
import { ProductService } from '../../core/product.service';

@Component({
  selector: 'app-products',
  templateUrl: './products.page.html',
  styleUrls: ['./products.page.scss'],
  imports: [
    RouterLink,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonCard,
    IonCardHeader,
    IonCardTitle,
    IonCardSubtitle,
    IonCardContent,
    IonButton,
    IonBadge,
  ],
})
export class ProductsPage {
  readonly products = signal<CatalogProduct[]>([]);
  readonly error = signal<string | null>(null);
  readonly online = signal(navigator.onLine);
  readonly cart = inject(CartService);
  readonly auth = inject(AuthService);
  private readonly productsApi = inject(ProductService);
  private readonly router = inject(Router);

  constructor() {
    window.addEventListener('online', () => this.online.set(true));
    window.addEventListener('offline', () => this.online.set(false));
  }

  ionViewWillEnter(): void {
    void this.cart.initialize();
    this.loadProducts();
  }

  add(product: CatalogProduct): void {
    if (!this.auth.isAuthenticated()) {
      void this.router.navigate(['/login']);
      return;
    }
    if (!this.hasPrice(product)) {
      this.error.set('Necesitás conexión para consultar el precio y agregar el producto.');
      return;
    }
    void this.cart.add(product);
  }

  hasPrice(product: CatalogProduct): product is Product {
    return 'price' in product && typeof product.price === 'number';
  }

  logout(): void {
    this.auth.logout();
    this.cart.reset();
    this.products.set([]);
    this.loadPublicProducts(false);
  }

  private loadProducts(): void {
    this.error.set(null);
    if (!this.auth.isAuthenticated()) {
      this.loadPublicProducts(false);
      return;
    }
    this.productsApi.listForMember().subscribe({
      next: (products) => this.products.set(products),
      error: () => this.loadPublicProducts(true),
    });
  }

  private loadPublicProducts(pricesUnavailable: boolean): void {
    this.productsApi.listPublic().subscribe({
      next: (products) => {
        this.products.set(products);
        this.error.set(pricesUnavailable ? 'Sin conexión: precios no disponibles.' : null);
      },
      error: () => this.error.set('No hay catálogo cacheado todavía. Conectate una vez para descargarlo.'),
    });
  }
}
