import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IonButton, IonCard, IonCardContent, IonCardHeader, IonCardTitle, IonContent, IonSpinner } from '@ionic/angular';
import { AdminService } from '../../core/admin.service';
import { AdminDashboard } from '../../core/models';

@Component({
  templateUrl: './dashboard.page.html',
  styleUrls: ['./admin-common.scss'],
  imports: [IonContent, IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonButton, IonSpinner, RouterLink],
})
export class AdminDashboardPage implements OnInit {
  readonly summary = signal<AdminDashboard | null>(null);
  readonly error = signal('');
  private readonly admin = inject(AdminService);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.error.set('');
    this.admin.dashboard().subscribe({
      next: (summary) => this.summary.set(summary),
      error: () => this.error.set('No se pudo cargar el resumen administrativo.'),
    });
  }
}
