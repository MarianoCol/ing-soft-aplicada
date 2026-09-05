import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { IonButton, IonButtons, IonHeader, IonRouterOutlet, IonTitle, IonToolbar } from '@ionic/angular';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-admin-layout',
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.scss'],
  imports: [IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonRouterOutlet, RouterLink, RouterLinkActive],
})
export class AdminLayoutComponent {
  readonly online = signal(navigator.onLine);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  constructor() {
    window.addEventListener('online', () => this.online.set(true));
    window.addEventListener('offline', () => this.online.set(false));
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/products');
  }
}
