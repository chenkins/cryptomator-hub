import { mount } from '@cypress/vue';
import { useI18n } from 'vue-i18n';
import CreateVault from '../../src/components/CreateVault.vue';

describe('CreateVault', () => {
  beforeEach(() => {
    cy.intercept('GET', '/setup', {
      statusCode: 200,
      body: {
        setupCompleted: true,
        keycloakUrl: 'url'
      },
    });
  });

  before(() => {
    cy.stub(useI18n, 'apply')
      .callsFake((_arg1) => {
        return { t: (key: string, ...args: any) => 'Foo' };
      });
  });

  it('is shown in cypress', () => {
    mount(CreateVault);
  });
});
