import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { IonBadge, IonButton, IonContent, IonItem, IonLabel, IonList, IonSelect, IonSelectOption } from '@ionic/angular';
import { AdminService } from '../../core/admin.service';
import { AdminOrderDetail, AdminOrderSummary, OrderStatus } from '../../core/models';

@Component({
  templateUrl: './admin-orders.page.html',
  styleUrls: ['./admin-common.scss'],
  imports: [CurrencyPipe, DatePipe, IonContent, IonSelect, IonSelectOption, IonList, IonItem, IonLabel, IonBadge, IonButton],
})
export class AdminOrdersPage implements OnInit {
  readonly orders = signal<AdminOrderSummary[]>([]);
  readonly selected = signal<AdminOrderDetail | null>(null);
  readonly total = signal(0);
  readonly error = signal('');
  readonly message = signal('');
  page = 0;
  status: '' | OrderStatus = '';
  private readonly admin = inject(AdminService);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.error.set('');
    this.admin.orders(this.page, this.status).subscribe({
      next: ({ items, total }) => { this.orders.set(items); this.total.set(total); },
      error: () => this.error.set('No se pudieron cargar los pedidos.'),
    });
  }

  open(order: AdminOrderSummary): void {
    this.admin.order(order.id).subscribe({
      next: (detail) => this.selected.set(detail),
      error: () => this.error.set('No se pudo cargar el detalle del pedido.'),
    });
  }

  change(status: 'COMPLETED' | 'CANCELLED'): void {
    const order = this.selected();
    if (!order || !confirm(`¿Cambiar el pedido #${order.id} a ${this.statusLabel(status)}?`)) return;
    this.admin.changeOrderStatus(order.id, status).subscribe({
      next: (updated) => { this.selected.set(updated); this.message.set('Estado actualizado.'); this.load(); },
      error: () => this.error.set('No se pudo cambiar el estado. Revisá el stock y el estado actual.'),
    });
  }

  statusLabel(status: OrderStatus): string {
    return ({ PENDING: 'Pendiente', COMPLETED: 'Completado', CANCELLED: 'Cancelado' } as const)[status];
  }

  previous(): void { if (this.page > 0) { this.page--; this.load(); } }
  next(): void { if ((this.page + 1) * 10 < this.total()) { this.page++; this.load(); } }
}
