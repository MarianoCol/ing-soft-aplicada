import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ProductService } from './product.service';

describe('ProductService', () => {
  let service: ProductService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [ProductService, provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ProductService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads the public catalog without using the member endpoint', () => {
    service.listPublic().subscribe();
    const request = http.expectOne((candidate) => candidate.url === '/api/products');
    expect(request.request.params.get('sort')).toBe('name,asc');
    request.flush([]);
  });

  it('loads priced products from the protected member endpoint', () => {
    service.listForMember().subscribe();
    const request = http.expectOne((candidate) => candidate.url === '/api/member/products');
    expect(request.request.params.get('size')).toBe('50');
    request.flush([]);
  });
});
