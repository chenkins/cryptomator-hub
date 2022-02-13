import AxiosStatic from 'axios';
import { Deferred, Maybe, MaybeType, None, Some } from './util';

const axios = AxiosStatic.create({
  baseURL: import.meta.env.DEV ? 'http://localhost:9090' : '',
  headers: {
    'Content-Type': 'application/json'
  }
});

export class ConfigDto {
  constructor(public setupCompleted: boolean, public keycloakUrl: string) { }
}

class ConfigWrapper {

  private loadPromise: Promise<void>;
  private data: Maybe<ConfigDto>;
  private deferredSetupCompletion: Deferred<void>;

  static build(): ConfigWrapper {
    return new ConfigWrapper();
  }

  private constructor() {
    this.data = None();
    this.deferredSetupCompletion = new Deferred();
    this.loadPromise = this.loadConfig();
    this.checkSetupCompleted();
  }

  private async loadConfig() {
    const response = await axios.get<ConfigDto>('/setup');
    this.data = Some(response.data); // no race condition due to java scripts event loop concurrency
  }

  public async get(): Promise<ConfigDto> {
    await this.loadPromise;
    return this.getUnsafe();
  }

  public getUnsafe(): ConfigDto {
    switch (this.data.type) {
      case MaybeType.None:
        throw new Error('Config not loaded');
      case MaybeType.Some:
        return this.data.value;
    }
  }

  private async checkSetupCompleted() {
    switch (this.data.type) {
      case MaybeType.None:
        break;
      case MaybeType.Some: {
        if (this.data.value.setupCompleted) {
          this.deferredSetupCompletion.resolve();
        }
        break;
      }
    }
  }

  public async reload(): Promise<void> {
    this.data = None();
    this.loadPromise = this.loadConfig();
    await this.loadPromise;
    this.checkSetupCompleted();
  }

  public awaitSetupCompletion(): Promise<void> {
    return this.deferredSetupCompletion.promise;
  }

}

const config = ConfigWrapper.build();

export default config;
