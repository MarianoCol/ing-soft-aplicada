describe('navegación de la tienda', () => {
  beforeEach(() => {
    cy.loginByApi(Cypress.env('username') ?? 'user', Cypress.env('password') ?? 'user');
  });

  it('mantiene el carrito con icono y la salida en las vistas autenticadas', () => {
    cy.visit('/products');
    cy.get('[data-cy="open-cart"] ion-icon').should('exist');
    cy.get('[data-cy="logout"]').should('exist');

    cy.visit('/cart');
    cy.get('[data-cy="open-cart"] ion-icon').should('exist');
    cy.get('[data-cy="logout"]').should('exist');

    cy.visit('/orders');
    cy.get('[data-cy="open-cart"] ion-icon').should('exist');
    cy.get('[data-cy="logout"]').should('exist');

    cy.visit('/products');
    cy.get('[data-cy="logout"]').click();
    cy.get('[data-cy="open-login"]').should('exist');
  });
});
