import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import TestEntityResolve from './route/test-entity-routing-resolve.service';

const testEntityRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/test-entity.component').then(m => m.TestEntityComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/test-entity-detail.component').then(m => m.TestEntityDetailComponent),
    resolve: {
      testEntity: TestEntityResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/test-entity-update.component').then(m => m.TestEntityUpdateComponent),
    resolve: {
      testEntity: TestEntityResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/test-entity-update.component').then(m => m.TestEntityUpdateComponent),
    resolve: {
      testEntity: TestEntityResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default testEntityRoute;
