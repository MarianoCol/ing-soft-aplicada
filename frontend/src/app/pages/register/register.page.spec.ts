import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { RegisterRequest } from '../../core/models';
import { RegisterPage } from './register.page';

@Component({ template: '', standalone: true })
class EmptyPage {}

describe('RegisterPage', () => {
  let fixture: ComponentFixture<RegisterPage>;
  let component: RegisterPage;
  let submittedRequest: RegisterRequest | undefined;

  beforeEach(() => {
    submittedRequest = undefined;
    TestBed.configureTestingModule({
      imports: [RegisterPage],
      providers: [
        provideRouter([{ path: 'products', component: EmptyPage }]),
        {
          provide: AuthService,
          useValue: {
            registerAndLogin: (request: RegisterRequest) => {
              submittedRequest = request;
              return of(true);
            },
          },
        },
      ],
    });
    fixture = TestBed.createComponent(RegisterPage);
    component = fixture.componentInstance;
  });

  it('submits valid minimal registration data', () => {
    component.login = 'new-user';
    component.email = 'new@example.com';
    component.password = 'secret';
    component.passwordConfirmation = 'secret';

    component.submit();

    expect(submittedRequest).toEqual({ login: 'new-user', email: 'new@example.com', password: 'secret' });
  });

  it('rejects mismatched passwords before calling the API', () => {
    component.login = 'new-user';
    component.email = 'new@example.com';
    component.password = 'secret';
    component.passwordConfirmation = 'different';

    component.submit();

    expect(submittedRequest).toBeUndefined();
    expect(component.passwordsDiffer()).toBe(true);
    expect(component.error()).toContain('Revisá');
  });
});
