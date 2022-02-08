import { mount } from '@cypress/vue';
import App from '../../src/App.vue';
import CreateVault from '../../src/components/CreateVault.vue';

const createApp = () => {
  return mount(App, {
    global: {

    }
  });
};

describe('CreateVault', () => {
  it('is shown in cypress', () => {
    const wrapper = createApp();
    mount(CreateVault);
  });
});
