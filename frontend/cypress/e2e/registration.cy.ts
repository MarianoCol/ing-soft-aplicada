describe('registro y precios protegidos', () => {
  let registeredLogin: string | undefined;

  afterEach(() => {
    if (!registeredLogin) return;
    cy.request<{ id_token: string }>({
      method: 'POST',
      url: `${Cypress.env('apiUrl')}/api/authenticate`,
      body: { username: 'admin', password: 'admin', rememberMe: true },
    }).then(({ body }) => {
      cy.request({
        method: 'DELETE',
        url: `${Cypress.env('apiUrl')}/api/admin/users/${registeredLogin}`,
        headers: { Authorization: `Bearer ${body.id_token}` },
        failOnStatusCode: false,
      });
    });
  });

  it('oculta precios al visitante y registra una cuenta con inicio de sesión automático', () => {
    registeredLogin = `user-${Date.now()}`;
    cy.intercept('GET', '**/api/products*').as('publicProducts');
    cy.visit('/products');
    cy.wait('@publicProducts').its('response.body.0').should('not.have.property', 'price');
    cy.get('[data-cy="product-price"]').should('not.exist');
    cy.get('[data-cy="product-price-hidden"]').should('exist');
    cy.get('[data-cy="open-login"]').should('exist');
    cy.get('[data-cy="open-register"]').click();

    cy.intercept('POST', '**/api/register').as('register');
    cy.intercept('POST', '**/api/authenticate').as('authenticate');
    cy.intercept('GET', '**/api/member/products*').as('memberProducts');
    cy.get('[data-cy="register-login"]').find('input').type(registeredLogin);
    cy.get('[data-cy="register-email"]').find('input').type(`${registeredLogin}@example.com`);
    cy.get('[data-cy="register-password"]').find('input').type('secret123');
    cy.get('[data-cy="register-password-confirmation"]').find('input').type('secret123');
    cy.get('[data-cy="register-submit"]').click();

    cy.wait('@register').its('response.statusCode').should('eq', 201);
    cy.wait('@authenticate').its('response.statusCode').should('eq', 200);
    cy.wait('@memberProducts').its('response.statusCode').should('eq', 200);
    cy.location('pathname').should('eq', '/products');
    cy.get('[data-cy="product-price"]').should('exist');
    cy.get('[data-cy="product-price-hidden"]').should('not.exist');
  });
});
