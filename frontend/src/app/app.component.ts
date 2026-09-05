import { Component, inject } from '@angular/core';
import { IonApp, IonRouterOutlet } from '@ionic/angular';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  imports: [IonApp, IonRouterOutlet],
})
export class AppComponent {
  private readonly auth = inject(AuthService);

  constructor() {
    if (this.auth.isAuthenticated()) {
      this.auth.loadAccount(true).subscribe();
    }
  }
}
