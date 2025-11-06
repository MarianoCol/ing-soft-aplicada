import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { ITestEntity } from '../test-entity.model';

@Component({
  selector: 'jhi-test-entity-detail',
  templateUrl: './test-entity-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class TestEntityDetailComponent {
  testEntity = input<ITestEntity | null>(null);

  previousState(): void {
    window.history.back();
  }
}
