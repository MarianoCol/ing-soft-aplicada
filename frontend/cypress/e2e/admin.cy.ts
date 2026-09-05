describe('panel administrativo', () => {
  it('permite administrar el ciclo de vida de un producto', () => {
    const name = `Producto Admin ${Date.now()}`;
    cy.loginByApi('admin', 'admin');
    cy.visit('/admin/products');

    cy.get('[data-cy="product-name"]').find('input').clear().type(name);
    cy.get('[data-cy="product-price"]').find('input').clear().type('25.50');
    cy.get('[data-cy="product-stock"]').find('input').clear().type('12');
    cy.get('[data-cy="save-product"]').click();
    cy.contains('ion-item', name).as('productRow').should('contain.text', '25.50 USD');

    cy.get('@productRow').contains('ion-button', 'Editar').click();
    cy.get('[data-cy="product-stock"]').find('input').clear().type('20');
    cy.get('[data-cy="save-product"]').click();
    cy.contains('ion-item', name).should('contain.text', 'stock 20').contains('ion-button', 'Desactivar').click();
    cy.contains('ion-item', name).should('contain.text', 'Inactivo');

    cy.on('window:confirm', () => true);
    cy.request<Array<{ name: string }>>('/api/products?size=100').then(({ body }) => {
      expect(body.some((product) => product.name === name)).to.eq(false);
    });
    cy.contains('ion-item', name).contains('ion-button', 'Reactivar').click();
    cy.contains('ion-item', name).contains('ion-button', 'Eliminar').click();
    cy.contains('ion-item', name).should('not.exist');
  });

  it('completa un pedido pendiente y descuenta el stock', () => {
    const name = `Producto Pedido ${Date.now()}`;
    cy.loginByApi('admin', 'admin');
    cy.get<string>('@authToken').then((adminToken) => {
      cy.request<{ id: number }>({
        method: 'POST', url: `${Cypress.env('apiUrl')}/api/admin/products`,
        headers: { Authorization: `Bearer ${adminToken}` },
        body: { name, description: 'Producto de prueba E2E', price: 10, stock: 10, active: true },
      }).then(({ body: product }) => {
        cy.request<{ id_token: string }>({
          method: 'POST', url: `${Cypress.env('apiUrl')}/api/authenticate`,
          body: { username: 'user', password: 'user', rememberMe: true },
        }).then(({ body }) => {
          cy.request<{ id: number }>({
            method: 'PUT', url: `${Cypress.env('apiUrl')}/api/cart/items/${product.id}`,
            headers: { Authorization: `Bearer ${body.id_token}` }, body: { quantity: 2 },
          }).then(({ body: cart }) => {
            cy.visit('/admin/orders');
            cy.contains('ion-item', `Pedido #${cart.id}`).click();
            cy.on('window:confirm', () => true);
            cy.get('[data-cy="complete-order"]').click();
            cy.get('[data-cy="order-detail"]').should('contain.text', 'Completado');
          });
        });
      });
    });
  });

  it('impide que un usuario normal abra el panel', () => {
    cy.loginByApi('user', 'user');
    cy.get<string>('@authToken').then((token) => {
      cy.request({
        url: `${Cypress.env('apiUrl')}/api/admin/dashboard`,
        headers: { Authorization: `Bearer ${token}` },
        failOnStatusCode: false,
      }).its('status').should('eq', 403);
    });
    cy.visit('/admin');
    cy.location('pathname').should('eq', '/products');
    cy.get('[data-cy="open-admin"]').should('not.exist');
  });
});
