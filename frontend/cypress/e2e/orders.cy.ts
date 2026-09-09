interface CartResponse {
  id: number;
  items: Array<{
    productId: number;
    productName: string;
    quantity: number;
  }>;
}

describe('historial de compras', () => {
  beforeEach(() => {
    cy.loginByApi(
      Cypress.env('username') ?? 'user',
      Cypress.env('password') ?? 'user',
    );
  });

  it('muestra en el historial una compra realizada por el usuario', () => {
    cy.get<string>('@authToken').then((token) => {
      cy.request<CartResponse>({
        method: 'PUT',
        url: `${Cypress.env('apiUrl')}/api/cart/items/1`,
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: {
          quantity: 1,
        },
      });
    });

    cy.intercept('POST', '**/api/cart/checkout').as('checkout');

    cy.intercept(
      'GET',
      /\/api\/member\/orders(\?.*)?$/,
    ).as('getOrders');

    cy.intercept(
      'GET',
      '**/api/member/orders/*',
    ).as('getOrderDetail');

    cy.visit('/cart');

    cy.get('[data-cy="cart-item-1"]')
      .should('contain.text', 'Producto E2E');

    cy.get('[data-cy="checkout"]').click();

    cy.wait('@checkout').then(({ response }) => {
      expect(response?.statusCode).to.eq(200);

      const orderId = response?.body.id as number;

      cy.contains(`Compra #${orderId} realizada correctamente`)
        .should('be.visible');

      cy.contains('ion-button', 'Mis compras').click();

      cy.wait('@getOrders')
        .its('response.statusCode')
        .should('eq', 200);

      cy.get(`[data-cy="member-order-${orderId}"]`)
        .should('contain.text', `Compra #${orderId}`)
        .and('contain.text', 'Completada')
        .click();

      cy.wait('@getOrderDetail')
        .its('response.statusCode')
        .should('eq', 200);

      cy.get('[data-cy="member-order-detail"]')
        .should('contain.text', 'Producto E2E')
        .and('contain.text', '19.99')
        .and('contain.text', 'Total');
    });
  });

  it('protege el historial cuando no existe una sesión', () => {
    cy.clearAllLocalStorage();
    cy.visit('/orders');

    cy.location('pathname').should('eq', '/login');
  });
});