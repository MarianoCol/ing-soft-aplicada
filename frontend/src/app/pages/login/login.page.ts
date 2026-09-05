import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
  IonButton,
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonLabel,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
  imports: [FormsModule, IonHeader, IonToolbar, IonTitle, IonContent, IonItem, IonInput, IonLabel, IonButton],
})
export class LoginPage {
  username = 'user';
  password = 'user';
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  submit(): void {
    this.loading.set(true);
    this.error.set(null);
    this.auth
      .login(this.username, this.password)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => void this.router.navigateByUrl(this.auth.isAdmin() ? '/admin' : '/products'),
        error: () => this.error.set('Usuario o contraseña incorrectos'),
      });
  }
}
