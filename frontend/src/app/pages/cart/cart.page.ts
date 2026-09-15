import { Component, inject, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import {
  IonBadge,
  IonButton,
  IonContent,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { addOutline, removeOutline } from 'ionicons/icons';
import { CartService } from '../../core/cart.service';
import { DisplayCartItem } from '../../core/models';
import { StoreHeaderComponent } from '../../shared/store-header.component';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.page.html',
  styleUrls: ['./cart.page.scss'],
  imports: [CurrencyPipe, StoreHeaderComponent, IonContent, IonList, IonItem, IonLabel, IonBadge, IonButton, IonIcon],
})
export class CartPage implements OnInit {
  readonly cart = inject(CartService);
  readonly icons = { addOutline, removeOutline };

  constructor() {
    addIcons(this.icons);
  }

  ngOnInit(): void {
    void this.cart.initialize();
  }

  decrease(item: DisplayCartItem): void {
    void this.cart.setQuantity(item, item.quantity - 1);
  }

  increase(item: DisplayCartItem): void {
    if (item.quantity < item.stock) {
      void this.cart.setQuantity(item, item.quantity + 1);
    }
  }

  checkout(): void {
    void this.cart.checkout();
  }
}
