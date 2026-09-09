import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  IonBadge,
  IonButton,
  IonContent,
  IonHeader,
  IonItem,
  IonLabel,
  IonList,
  IonSpinner,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { OrderDetail, OrderStatus, OrderSummary } from '../../core/models';
import { OrderService } from '../../core/order.service';

@Component({
  selector: 'app-orders',
  templateUrl: './orders.page.html',
  styleUrls: ['./orders.page.scss'],
  imports: [
    CurrencyPipe,
    DatePipe,
    RouterLink,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonButton,
    IonList,
    IonItem,
    IonLabel,
    IonBadge,
    IonSpinner,
  ],
})
export class OrdersPage implements OnInit {
  readonly orders = signal<OrderSummary[]>([]);
  readonly selected = signal<OrderDetail | null>(null);
  readonly total = signal(0);
  readonly loading = signal(false);
  readonly error = signal('');

  readonly pageSize = 10;
  page = 0;

  private readonly orderService = inject(OrderService);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');

    this.orderService.orders(this.page).subscribe({
      next: ({ items, total }) => {
        this.orders.set(items);
        this.total.set(total);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar tu historial de compras.');
        this.loading.set(false);
      },
    });
  }

  open(order: OrderSummary): void {
    this.error.set('');

    this.orderService.order(order.id).subscribe({
      next: (detail) => this.selected.set(detail),
      error: () =>
        this.error.set('No se pudo cargar el detalle de la compra.'),
    });
  }

  previous(): void {
    if (this.page > 0) {
      this.page--;
      this.selected.set(null);
      this.load();
    }
  }

  next(): void {
    if ((this.page + 1) * this.pageSize < this.total()) {
      this.page++;
      this.selected.set(null);
      this.load();
    }
  }

  statusLabel(status: OrderStatus): string {
    return {
      PENDING: 'Pendiente',
      COMPLETED: 'Completada',
      CANCELLED: 'Cancelada',
    }[status];
  }

  statusColor(status: OrderStatus): string {
    return {
      PENDING: 'warning',
      COMPLETED: 'success',
      CANCELLED: 'medium',
    }[status];
  }
}