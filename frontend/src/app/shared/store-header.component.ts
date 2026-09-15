import { Component, inject, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import {
  IonBadge,
  IonButton,
  IonButtons,
  IonHeader,
  IonIcon,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import {
  cartOutline,
  logInOutline,
  logOutOutline,
  personAddOutline,
  receiptOutline,
  settingsOutline,
  storefrontOutline,
} from 'ionicons/icons';
import { AuthService } from '../core/auth.service';
import { CartService } from '../core/cart.service';
import { SessionService } from '../core/session.service';

@Component({
  selector: 'app-store-header',
  templateUrl: './store-header.component.html',
  styleUrls: ['./store-header.component.scss'],
  imports: [
    RouterLink,
    RouterLinkActive,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonBadge,
    IonIcon,
  ],
})
export class StoreHeaderComponent {
  readonly title = input.required<string>();
  readonly auth = inject(AuthService);
  readonly cart = inject(CartService);
  private readonly session = inject(SessionService);

  readonly icons = {
    cartOutline,
    logInOutline,
    logOutOutline,
    personAddOutline,
    receiptOutline,
    settingsOutline,
    storefrontOutline,
  };

  constructor() {
    addIcons(this.icons);
  }

  logout(): void {
    this.session.logout();
  }
}
