import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { IonButton, IonContent, IonHeader, IonInput, IonItem, IonLabel, IonTitle, IonToolbar } from '@ionic/angular';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.page.html',
  styleUrls: ['./register.page.scss'],
  imports: [FormsModule, RouterLink, IonHeader, IonToolbar, IonTitle, IonContent, IonItem, IonInput, IonLabel, IonButton],
})
export class RegisterPage {
  login = '';
  email = '';
  password = '';
  passwordConfirmation = '';
  readonly loading = signal(false);
  readonly submitted = signal(false);
  readonly error = signal<string | null>(null);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  submit(): void {
    this.submitted.set(true);
    this.error.set(null);
    if (!this.isValid()) {
      this.error.set('Revisá el usuario, el email y la contraseña.');
      return;
    }

    const login = this.login.trim();
    this.loading.set(true);
    this.auth
      .registerAndLogin({ login, email: this.email.trim(), password: this.password })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (authenticated) => {
          if (authenticated) {
            void this.router.navigateByUrl('/products');
          } else {
            void this.router.navigate(['/login'], { queryParams: { registered: true, username: login } });
          }
        },
        error: (response: HttpErrorResponse) => this.error.set(this.registrationError(response)),
      });
  }

  passwordsDiffer(): boolean {
    return this.passwordConfirmation.length > 0 && this.password !== this.passwordConfirmation;
  }

  private isValid(): boolean {
    const loginPattern = /^[_.@A-Za-z0-9-]+$/;
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return (
      loginPattern.test(this.login.trim()) &&
      this.login.trim().length <= 50 &&
      emailPattern.test(this.email.trim()) &&
      this.email.trim().length <= 254 &&
      this.password.length >= 4 &&
      this.password.length <= 100 &&
      this.password === this.passwordConfirmation
    );
  }

  private registrationError(response: HttpErrorResponse): string {
    const type = String(response.error?.type ?? '');
    if (type.endsWith('/login-already-used')) return 'Ese nombre de usuario ya está en uso';
    if (type.endsWith('/email-already-used')) return 'Ese email ya está registrado';
    return 'No se pudo crear la cuenta. Revisá los datos e intentá nuevamente.';
  }
}
