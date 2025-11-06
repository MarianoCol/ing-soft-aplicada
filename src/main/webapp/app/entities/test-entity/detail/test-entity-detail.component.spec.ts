import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { TestEntityDetailComponent } from './test-entity-detail.component';

describe('TestEntity Management Detail Component', () => {
  let comp: TestEntityDetailComponent;
  let fixture: ComponentFixture<TestEntityDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestEntityDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./test-entity-detail.component').then(m => m.TestEntityDetailComponent),
              resolve: { testEntity: () => of({ id: 18625 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(TestEntityDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TestEntityDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load testEntity on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', TestEntityDetailComponent);

      // THEN
      expect(instance.testEntity()).toEqual(expect.objectContaining({ id: 18625 }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
