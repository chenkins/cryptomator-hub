import AxiosStatic, { AxiosRequestConfig, AxiosResponse } from 'axios';
import authPromise from './auth';
import { backendBaseURL } from './config';
import { VaultKeys } from './crypto';
import { JWTHeader } from './jwt';

const axiosBaseCfg: AxiosRequestConfig = {
  baseURL: backendBaseURL,
  headers: {
    'Content-Type': 'application/json'
  }
};

const axiosAuth = AxiosStatic.create(axiosBaseCfg);
axiosAuth.interceptors.request.use(async request => {
  try {
    const token = await authPromise.then(auth => auth.bearerToken());
    if (request.headers) {
      request.headers['Authorization'] = `Bearer ${token}`;
    } else {
      request.headers = { 'Authorization': `Bearer ${token}` };
    }
    return request;
  } catch (err: unknown) {
    // only things from auth module can throw errors here
    throw new UnauthorizedError();
  }
});

const vaultAdminAuthorizationJWTLeeway = 15;
/* DTOs */

export class VaultDto {

  constructor(public id: string, public name: string, public description: string, public creationTime: Date, public masterkey: string, public iterations: number, public salt: string, public authPublicKey: string, public authPrivateKey: string) { }
}

export class DeviceDto {
  constructor(public id: string, public name: string, public publicKey: string, public accessTo: VaultDto[], public creationTime: Date) { }
}

export class AuthorityDto {
  constructor(public id: string, public name: string, public type: string, public pictureUrl: string) { }
}

export class UserDto extends AuthorityDto {
  constructor(public id: string, public name: string, public pictureUrl: string, public email: string, public devices: DeviceDto[]) {
    super(id, name, 'user', pictureUrl);
  }
}

export class GroupDto extends AuthorityDto {
  constructor(public id: string, public name: string, public pictureUrl: string) {
    super(id, name, 'group', pictureUrl);
  }
}

export class BillingDto {
  constructor(public hubId: string, public hasLicense: boolean, public email: string, public totalSeats: number, public remainingSeats: number, public issuedAt: Date, public expiresAt: Date) { }
}

export class VersionDto {
  constructor(public hubVersion: string, public keycloakVersion: string) { }
}

/* Services */

export interface VaultIdHeader extends JWTHeader {
  vaultId: string;
}

class VaultService {
  public async listSharedOrOwned(): Promise<VaultDto[]> {
    return axiosAuth.get('/vaults').then(response => response.data);
  }

  public async get(vaultId: string): Promise<VaultDto> {
    return axiosAuth.get(`/vaults/${vaultId}`)
      .then(response => {
        let dateString = response.data.creationTime;
        response.data.creationTime = new Date(dateString);
        return response.data;
      })
      .catch((err) => rethrowAndConvertIfExpected(err, 404));
  }

  public async getMembers(vaultId: string, vaultKeys: VaultKeys): Promise<AuthorityDto[]> {
    let vaultAdminAuthorizationJWT = await this.buildVaultAdminAuthorizationJWT(vaultId, vaultKeys);
    return axiosAuth.get(`/vaults/${vaultId}/members`, { headers: { 'Cryptomator-Vault-Admin-Authorization': vaultAdminAuthorizationJWT } })
      .then(response => response.data).catch(err => rethrowAndConvertIfExpected(err, 403));
  }

  public async addUser(vaultId: string, userId: string, vaultKeys: VaultKeys): Promise<AxiosResponse<void>> {
    let vaultAdminAuthorizationJWT = await this.buildVaultAdminAuthorizationJWT(vaultId, vaultKeys);
    return axiosAuth.put(`/vaults/${vaultId}/users/${userId}`, null, { headers: { 'Cryptomator-Vault-Admin-Authorization': vaultAdminAuthorizationJWT } })
      .catch((err) => rethrowAndConvertIfExpected(err, 404, 409));
  }

  public async addGroup(vaultId: string, groupId: string, vaultKeys: VaultKeys): Promise<AxiosResponse<void>> {
    let vaultAdminAuthorizationJWT = await this.buildVaultAdminAuthorizationJWT(vaultId, vaultKeys);
    return axiosAuth.put(`/vaults/${vaultId}/groups/${groupId}`, null, { headers: { 'Cryptomator-Vault-Admin-Authorization': vaultAdminAuthorizationJWT } })
      .catch((err) => rethrowAndConvertIfExpected(err, 404, 409));
  }

  public async getDevicesRequiringAccessGrant(vaultId: string, vaultKeys: VaultKeys): Promise<DeviceDto[]> {
    let vaultAdminAuthorizationJWT = await this.buildVaultAdminAuthorizationJWT(vaultId, vaultKeys);
    return axiosAuth.get(`/vaults/${vaultId}/devices-requiring-access-grant`, { headers: { 'Cryptomator-Vault-Admin-Authorization': vaultAdminAuthorizationJWT } })
      .then(response => response.data).catch(err => rethrowAndConvertIfExpected(err, 403));
  }

  public async createVault(vaultId: string, name: string, description: string, masterkey: string, iterations: number, salt: string, signPubKey: string, signPrvKey: string): Promise<AxiosResponse<any>> {
    const body: VaultDto = { id: vaultId, name: name, description: description, creationTime: new Date(), masterkey: masterkey, iterations: iterations, salt: salt, authPublicKey: signPubKey, authPrivateKey: signPrvKey };
    return axiosAuth.put(`/vaults/${vaultId}`, body)
      .catch((err) => rethrowAndConvertIfExpected(err, 404, 409));
  }

  public async grantAccess(vaultId: string, deviceId: string, jwe: string, vaultKeys: VaultKeys) {
    let vaultAdminAuthorizationJWT = await this.buildVaultAdminAuthorizationJWT(vaultId, vaultKeys);
    await axiosAuth.put(`/vaults/${vaultId}/keys/${deviceId}`, jwe, { headers: { 'Content-Type': 'text/plain', 'Cryptomator-Vault-Admin-Authorization': vaultAdminAuthorizationJWT } })
      .catch((err) => rethrowAndConvertIfExpected(err, 404, 409));
  }

  public async revokeUserAccess(vaultId: string, userId: string, vaultKeys: VaultKeys) {
    let vaultAdminAuthorizationJWT = await this.buildVaultAdminAuthorizationJWT(vaultId, vaultKeys);
    await axiosAuth.delete(`/vaults/${vaultId}/users/${userId}`, { headers: { 'Cryptomator-Vault-Admin-Authorization': vaultAdminAuthorizationJWT } })
      .catch((err) => rethrowAndConvertIfExpected(err, 404));
  }

  private async buildVaultAdminAuthorizationJWT(vaultId: string, vaultKeys: VaultKeys): Promise<string> {
    let vaultIdHeader: VaultIdHeader = { alg: 'ES384', b64: true, typ: 'JWT', vaultId: vaultId };
    let nowInSeconds = this.secondsSinceEpoch();
    let jwtPayload = { exp: nowInSeconds + vaultAdminAuthorizationJWTLeeway, nbf: nowInSeconds - vaultAdminAuthorizationJWTLeeway, iat: nowInSeconds };
    return vaultKeys.signVaultEditRequest(vaultIdHeader, jwtPayload);
  }

  private secondsSinceEpoch(): number {
    return Math.floor(Date.now() / 1000);
  }
}
class DeviceService {

  public async removeDevice(deviceId: string): Promise<AxiosResponse<any>> {
    return axiosAuth.delete(`/devices/${deviceId}`)
      .catch((err) => rethrowAndConvertIfExpected(err, 404));
  }

}

class UserService {

  public async syncMe(): Promise<void> {
    return axiosAuth.put('/users/me');
  }
  public async me(withDevices: boolean = false, withAccessibleVaults: boolean = false): Promise<UserDto> {
    return axiosAuth.get<UserDto>(`/users/me?withDevices=${withDevices}&withAccessibleVaults=${withAccessibleVaults}`).then(response => response.data);
  }

  public async listAll(): Promise<UserDto[]> {
    return axiosAuth.get<UserDto[]>('/users/').then(response => response.data);
  }

}

class AuthorityService {
  public async search(query: string): Promise<AuthorityDto[]> {
    return axiosAuth.get<AuthorityDto[]>(`/authorities/search?query=${query}`).then(response => response.data);
  }

}

class BillingService {
  public async get(): Promise<BillingDto> {
    return axiosAuth.get('/billing').then(response => {
      response.data.issuedAt = new Date(response.data.issuedAt);
      response.data.expiresAt = new Date(response.data.expiresAt);
      return response.data;
    });
  }

  public async setToken(token: string): Promise<void> {
    return axiosAuth.put('/billing/token', token, { headers: { 'Content-Type': 'text/plain' } });
  }
}

class VersionService {
  public async get(): Promise<VersionDto> {
    return axiosAuth.get<VersionDto>('/version').then(response => response.data);
  }
}

/**
 * Note: Each service can thrown an {@link UnauthorizedError} when the access token is expired!
 */
const services = {
  vaults: new VaultService(),
  users: new UserService(),
  authorities: new AuthorityService(),
  devices: new DeviceService(),
  billing: new BillingService(),
  version: new VersionService()
};

function convertExpectedToBackendError(status: number): BackendError {
  switch (status) {
    case 403:
      return new ForbiddenError();
    case 404:
      return new NotFoundError();
    case 409:
      return new ConflictError();
    default:
      return new BackendError('Status Code ${status} not mapped');
  }
}

/**
 * Rethrows the error object or, if 'error' is an response with an expected http status code, it is converted to an BackendError and then rethrown.
 * @param error A thrown object
 * @param expectedStatusCodes The expected http status codes of the backend call
 */
function rethrowAndConvertIfExpected(error: unknown, ...expectedStatusCodes: number[]): Promise<any> {
  if (AxiosStatic.isAxiosError(error) && error.response != null && expectedStatusCodes.includes(error.response.status)) {
    throw convertExpectedToBackendError(error.response.status);
  } else {
    throw error;
  }
}

export default services;

//-- Error thrown by this module --
export class BackendError extends Error {
  constructor(msg: string) {
    super(msg);
  }
}

export class UnauthorizedError extends BackendError {
  constructor() {
    super('Unauthorized');
  }
}

export class ForbiddenError extends BackendError {
  constructor() {
    super('Not authorized to access resource');
  }
}

export class NotFoundError extends BackendError {
  constructor() {
    super('Requested resource not found');
  }
}

export class ConflictError extends BackendError {
  constructor() {
    super('Resource already exists');
  }
}
