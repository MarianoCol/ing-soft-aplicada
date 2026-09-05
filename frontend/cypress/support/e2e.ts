declare global {
  namespace Cypress {
    interface Chainable {
      loginByApi(username: string, password: string): Chainable<void>;
    }
  }
}

Cypress.Commands.add('loginByApi', (username: string, password: string) => {
  cy.session(
    username,
    () => {
      cy.request({
        method: 'POST',
        url: `${Cypress.env('apiUrl')}/api/authenticate`,
        body: { username, password, rememberMe: true },
      }).then(({ body }) => {
        cy.visit('/login', {
          onBeforeLoad(win) {
            win.localStorage.setItem('shopping_cart_token', body.id_token);
            win.localStorage.setItem('shopping_cart_user', username);
          },
        });
      });
    },
    {
      validate() {
        cy.visit('/login');
        cy.window().its('localStorage.shopping_cart_token').should('be.a', 'string').and('not.be.empty');
      },
    },
  );
  cy.visit('/login');
  cy.window().then((win) => {
    cy.wrap(win.localStorage.getItem('shopping_cart_token')).as('authToken');
  });
});

export {};
