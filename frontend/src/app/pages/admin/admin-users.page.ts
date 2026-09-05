import { Component, inject, OnInit, signal } from '@angular/core';
import { IonBadge, IonButton, IonContent, IonItem, IonLabel, IonList } from '@ionic/angular';
import { AdminService } from '../../core/admin.service';
import { AuthService } from '../../core/auth.service';
import { AdminUser } from '../../core/models';

@Component({
  templateUrl: './admin-users.page.html',
  styleUrls: ['./admin-common.scss'],
  imports: [IonContent, IonList, IonItem, IonLabel, IonBadge, IonButton],
})
export class AdminUsersPage implements OnInit {
  readonly users = signal<AdminUser[]>([]);
  readonly total = signal(0);
  readonly error = signal('');
  readonly message = signal('');
  readonly auth = inject(AuthService);
  page = 0;
  private readonly admin = inject(AdminService);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.error.set('');
    this.admin.users(this.page).subscribe({
      next: ({ items, total }) => { this.users.set(items); this.total.set(total); },
      error: () => this.error.set('No se pudieron cargar los usuarios.'),
    });
  }

  toggleActive(user: AdminUser): void {
    if (this.isCurrent(user)) return;
    this.save({ ...user, activated: !user.activated }, user.activated ? 'Usuario desactivado.' : 'Usuario activado.');
  }

  toggleAdmin(user: AdminUser): void {
    if (this.isCurrent(user)) return;
    const authorities = this.isAdmin(user)
      ? user.authorities.filter((authority) => authority !== 'ROLE_ADMIN')
      : [...new Set([...user.authorities, 'ROLE_USER', 'ROLE_ADMIN'])];
    this.save({ ...user, authorities }, this.isAdmin(user) ? 'Rol administrador removido.' : 'Rol administrador asignado.');
  }

  isAdmin(user: AdminUser): boolean { return user.authorities.includes('ROLE_ADMIN'); }
  isCurrent(user: AdminUser): boolean { return user.login.toLowerCase() === this.auth.username()?.toLowerCase(); }
  previous(): void { if (this.page > 0) { this.page--; this.load(); } }
  next(): void { if ((this.page + 1) * 10 < this.total()) { this.page++; this.load(); } }

  private save(user: AdminUser, message: string): void {
    this.admin.updateUser(user).subscribe({
      next: () => { this.message.set(message); this.load(); },
      error: () => this.error.set('No se pudo modificar el usuario.'),
    });
  }
}
