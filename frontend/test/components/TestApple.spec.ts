//import { mount } from '@cypress/vue';
import config from '../../src/common/config';
//import TestApple from '../../src/components/TestApple.vue';

describe('TestApple', () => {

  beforeEach(() => {
    cy.intercept('GET', 'http://localhost:9090/setup', {
      statusCode: 200,
      body: {
        setupCompleted: true,
        keycloakUrl: 'urlÄ'
      },
    });
  });

  it('test is shown in cypress', () => {
  });

  it('config can be imported', () => {
    let obj = config.get();
    console.log(obj);
  });
});
