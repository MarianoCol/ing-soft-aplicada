interface CartResponse {
  items: Array<{
    productId: number;
    productName: string;
    quantity: number;
  }>;
}

describe('carrito de compras', () => {
  beforeEach(() => {
    cy.loginByApi(Cypress.env('username') ?? 'user', Cypress.env('password') ?? 'user');
    cy.get<string>('@authToken').then((token) => {
      cy.request<CartResponse>({
        method: 'PUT',
        url: `${Cypress.env('apiUrl')}/api/cart/items/1`,
        headers: { Authorization: `Bearer ${token}` },
        body: { quantity: 1 },
      })
        .its('body')
        .as('initialCart');
    });
  });

  it('inicia sesión por API y agrega el producto determinista desde la interfaz', () => {
    cy.intercept('GET', '**/api/member/products*').as('getProducts');
    cy.intercept('GET', '**/api/cart').as('getCart');
    cy.intercept('PUT', '**/api/cart/items/1').as('setQuantity');

    cy.visit('/products');
    cy.wait('@getProducts').its('response.statusCode').should('eq', 200);
    cy.wait('@getCart').its('response.statusCode').should('eq', 200);

    cy.get<CartResponse>('@initialCart').then((initialCart) => {
      const previousCount = initialCart.items.reduce((sum, item) => sum + item.quantity, 0);
      cy.get('[data-cy="cart-count"]').should('have.text', String(previousCount));
      cy.get('[data-cy="product-1"]').should('contain.text', 'Producto E2E');
      cy.get('[data-cy="add-product-1"]').click();
      cy.wait('@setQuantity').its('response.statusCode').should('eq', 200);
      cy.get('[data-cy="cart-count"]').should('have.text', String(previousCount + 1));
    });

    cy.get<string>('@authToken').then((token) => {
      cy.request({
        url: `${Cypress.env('apiUrl')}/api/cart`,
        headers: { Authorization: `Bearer ${token}` },
      }).then(({ status, body }) => {
        expect(status).to.eq(200);
        expect(body.items.find((item: { productId: number }) => item.productId === 1)).to.include({
          productId: 1,
          productName: 'Producto E2E',
          quantity: 2,
        });
      });
    });

    cy.visit('/cart');
    cy.get('[data-cy="cart-item-1"]').should('contain.text', 'Producto E2E');
  });
});
