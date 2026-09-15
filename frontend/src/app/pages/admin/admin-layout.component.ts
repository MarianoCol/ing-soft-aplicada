import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { IonButton, IonButtons, IonHeader, IonRouterOutlet, IonTitle, IonToolbar } from '@ionic/angular';
import { SessionService } from '../../core/session.service';

@Component({
  selector: 'app-admin-layout',
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.scss'],
  imports: [IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonRouterOutlet, RouterLink, RouterLinkActive],
})
export class AdminLayoutComponent {
  readonly online = signal(navigator.onLine);
  private readonly session = inject(SessionService);

  constructor() {
    window.addEventListener('online', () => this.online.set(true));
    window.addEventListener('offline', () => this.online.set(false));
  }

  logout(): void {
    this.session.logout();
  }
}
