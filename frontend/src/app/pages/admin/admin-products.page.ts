import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  IonBadge,
  IonButton,
  IonContent,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonSearchbar,
  IonSelect,
  IonSelectOption,
  IonTextarea,
} from '@ionic/angular';
import { AdminService } from '../../core/admin.service';
import { AdminProduct } from '../../core/models';

interface ProductDraft { name: string; description: string; price: number; stock: number; }

@Component({
  templateUrl: './admin-products.page.html',
  styleUrls: ['./admin-common.scss'],
  imports: [
    FormsModule, IonContent, IonButton, IonSearchbar, IonSelect, IonSelectOption, IonList, IonItem, IonLabel,
    IonBadge, IonInput, IonTextarea,
  ],
})
export class AdminProductsPage implements OnInit {
  readonly products = signal<AdminProduct[]>([]);
  readonly total = signal(0);
  readonly error = signal('');
  readonly message = signal('');
  readonly editing = signal<AdminProduct | null>(null);
  page = 0;
  search = '';
  active: '' | 'true' | 'false' = '';
  draft: ProductDraft = this.emptyDraft();
  private readonly admin = inject(AdminService);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.error.set('');
    this.admin.products(this.page, this.search, this.active).subscribe({
      next: ({ items, total }) => { this.products.set(items); this.total.set(total); },
      error: () => this.error.set('No se pudieron cargar los productos.'),
    });
  }

  newProduct(): void { this.editing.set(null); this.draft = this.emptyDraft(); this.message.set(''); }

  edit(product: AdminProduct): void {
    this.editing.set(product);
    this.draft = { name: product.name, description: product.description ?? '', price: product.price, stock: product.stock };
    this.message.set('');
  }

  save(): void {
    if (!this.draft.name.trim() || this.draft.price < 0 || this.draft.stock < 0) {
      this.error.set('Revisá el nombre, precio y stock.');
      return;
    }
    const current = this.editing();
    const request = current
      ? this.admin.updateProduct({ ...current, ...this.draft, name: this.draft.name.trim() })
      : this.admin.createProduct({ ...this.draft, name: this.draft.name.trim(), active: true });
    request.subscribe({
      next: () => { this.newProduct(); this.message.set(current ? 'Producto actualizado.' : 'Producto creado.'); this.load(); },
      error: () => this.error.set('No se pudo guardar el producto.'),
    });
  }

  toggle(product: AdminProduct): void {
    this.admin.setProductActive(product.id, !product.active).subscribe({
      next: () => { this.message.set(product.active ? 'Producto desactivado.' : 'Producto reactivado.'); this.load(); },
      error: () => this.error.set('No se pudo cambiar el estado del producto.'),
    });
  }

  remove(product: AdminProduct): void {
    if (!product.deletable || !confirm(`¿Eliminar definitivamente “${product.name}”?`)) return;
    this.admin.deleteProduct(product.id).subscribe({
      next: () => { this.message.set('Producto eliminado definitivamente.'); this.load(); },
      error: () => this.error.set('El producto tiene historial y no puede eliminarse; desactivalo.'),
    });
  }

  previous(): void { if (this.page > 0) { this.page--; this.load(); } }
  next(): void { if ((this.page + 1) * 10 < this.total()) { this.page++; this.load(); } }

  private emptyDraft(): ProductDraft { return { name: '', description: '', price: 0, stock: 0 }; }
}
