import { Component, inject, OnInit, signal } from '@angular/core';
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
import { Product } from '../../core/models';
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
export class ProductsPage implements OnInit {
  readonly products = signal<Product[]>([]);
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

  ngOnInit(): void {
    void this.cart.initialize();
    this.productsApi.list().subscribe({
      next: (products) => this.products.set(products),
      error: () => this.error.set('No hay catálogo cacheado todavía. Conectate una vez para descargarlo.'),
    });
  }

  add(product: Product): void {
    if (!this.auth.isAuthenticated()) {
      void this.router.navigate(['/login']);
      return;
    }
    void this.cart.add(product);
  }

  logout(): void {
    this.auth.logout();
    this.cart.reset();
    void this.router.navigateByUrl('/products');
  }
}
