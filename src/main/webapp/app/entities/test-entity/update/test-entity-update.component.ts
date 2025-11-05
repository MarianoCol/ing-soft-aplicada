import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ITestEntity } from '../test-entity.model';
import { TestEntityService } from '../service/test-entity.service';
import { TestEntityFormGroup, TestEntityFormService } from './test-entity-form.service';

@Component({
  selector: 'jhi-test-entity-update',
  templateUrl: './test-entity-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class TestEntityUpdateComponent implements OnInit {
  isSaving = false;
  testEntity: ITestEntity | null = null;

  protected testEntityService = inject(TestEntityService);
  protected testEntityFormService = inject(TestEntityFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: TestEntityFormGroup = this.testEntityFormService.createTestEntityFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ testEntity }) => {
      this.testEntity = testEntity;
      if (testEntity) {
        this.updateForm(testEntity);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const testEntity = this.testEntityFormService.getTestEntity(this.editForm);
    if (testEntity.id !== null) {
      this.subscribeToSaveResponse(this.testEntityService.update(testEntity));
    } else {
      this.subscribeToSaveResponse(this.testEntityService.create(testEntity));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ITestEntity>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(testEntity: ITestEntity): void {
    this.testEntity = testEntity;
    this.testEntityFormService.resetForm(this.editForm, testEntity);
  }
}
