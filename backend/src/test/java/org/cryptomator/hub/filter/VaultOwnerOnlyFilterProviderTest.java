package org.cryptomator.hub.filter;

import com.radcortez.flyway.test.annotation.DataSource;
import com.radcortez.flyway.test.annotation.FlywayTest;
import io.agroal.api.AgroalDataSource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;

@QuarkusTest
@FlywayTest(value = @DataSource(url = "jdbc:h2:mem:test"), additionalLocations = {"classpath:org/cryptomator/hub/flyway"})
class VaultOwnerOnlyFilterProviderTest {

	private static final String VALID_TOKEN_VAULT_2 = "eyJhbGciOiJFUzM4NCIsInR5cCI6IkpXVCIsInZhdWx0SWQiOiJ2YXVsdDIifQ.e30.wGUvw1owDrVXlha056Hb1CRO6IQU8x4aadk5IGlaB12iFXZaypDATCCWGgEV3s2Q9qSVrY9A7M-g0a4FBK4DJ06u8t02Igj8Bh1Ba3jOOdAuiGslttSAcfMqImcjkRZL";
	private static final String VALID_TOKEN_VAULT_3000 = "eyJhbGciOiJFUzM4NCIsInR5cCI6IkpXVCIsInZhdWx0SWQiOiJ2YXVsdDMwMDAifQ.eyJpYXQiOjE1MTYyMzkwMjIsImV4cCI6MTUxNjIzOTAyMn0.ByF9Y3A2w7lRWCEGH4C0tTxME1HM5941BF_IKsd-pY_FF1AYliEFcRMPp6yZSpPXs7T_hrKWViXKQbTyhyEZuQPG1YOy4KUYZEpl0POlWT8iruWTmIdJ_LB0As8d2HJM";
	private static final String EXPIRED_TOKEN = "eyJhbGciOiJFUzM4NCIsInR5cCI6IkpXVCIsInZhdWx0SWQiOiJ2YXVsdDIifQ.eyJpYXQiOjE1MTYyMzkwMjIsImV4cCI6MTUxNjIzOTAyMn0.fMeWvzSfwfxLXnsMjYg0EMKbdt6FSzc86g_btgJERrrv9DKMcj7rb-X0MbXjbE0albxmc0Llr2p348Fi1vJO0pl0ldcwCeV1Dn8BFpkLKE08WVbE4sLWPHh2PmgTTd-F";
	private static final String TOKEN_WITH_INVALID_SIGNATURE = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzM4NCIsInZhdWx0SWQiOiJ2YXVsdDIifQ.e30.cGZDCqzJQgcBHNVPcmBc8JfeGzUf3CHUrwSAMwOA0Dcy9aUZvsAm1dr1MKzuPW_UFHRfMnNi2EwASOA6t-vPWvPFolAHFn5REt2Y9Aw9mIz-qxSBLpz6OMZD16tysQcd";
	private static final String TOKEN_WITHOUT_VAULT_ID = "eyJhbGciOiJFUzM4NCIsInR5cCI6IkpXVCJ9.e30.vT0jNCotwtzr37JNM_C6uZFCw3GvVjcikn-CVrDociILPiXBXA8i7dWFwBnUQkDBcFbouh-eUB_wEWgqe9WTG2rT66_c1G2LZUQcCsKdWJdTyK4ZxLXLYOYhNOHtqShI";
	private static final String TOKEN_WITH_INVALID_VAULT_ID = "eyJhbGciOiJFUzM4NCIsInR5cCI6IkpXVCIsInZhdWx0SWQiOjI1fQ.e30.YxqmX5xeOviP9WldQV870zhPEF4PRaZrW0TaoWzm4lvkEmacIUt3OIoH0grAeh_gtJNRg4WfnqFNTgUx40-yDOtBLzyoeubfrMgb0-agN1898Mbr4ZhD1xqor0lBDrmc";
	private static final String MALFORMED_TOKEN = "hello world";

	private VaultOwnerOnlyFilterProvider vaultOwnerOnlyFilterProvider;
	private ContainerRequestContext context;
	private UriInfo uriInfo;

	@Inject
	AgroalDataSource dataSource;

	@BeforeEach
	void setUp() {
		vaultOwnerOnlyFilterProvider = new VaultOwnerOnlyFilterProvider();
		context = Mockito.mock(ContainerRequestContext.class);
		uriInfo = Mockito.mock(UriInfo.class);
	}

	@Test
	@DisplayName("validate valid Client-Jwt header")
	public void testValidClientJwtHeader() {
		var pathParams = new MultivaluedHashMap<String, String>();
		pathParams.add("vaultId", "vault2");

		Mockito.when(context.getHeaderString("Client-Jwt")).thenReturn(VALID_TOKEN_VAULT_2);
		Mockito.when(context.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(context.getUriInfo().getPathParameters()).thenReturn(pathParams);

		vaultOwnerOnlyFilterProvider.filter(context);
	}

	@Test
	@DisplayName("validate no Client-Jwt header provided")
	public void testNoClientJwtHeader() {
		var pathParams = new MultivaluedHashMap<String, String>();
		pathParams.add("vaultId", "vault2");

		Mockito.when(context.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(context.getUriInfo().getPathParameters()).thenReturn(pathParams);

		Assertions.assertThrows(VaultOwnerNotProvidedException.class, () -> vaultOwnerOnlyFilterProvider.filter(context));
	}

	@Test
	@DisplayName("validate other path-param provided")
	public void testOtherPathParamProvided() {
		var pathParams = new MultivaluedHashMap<String, String>();
		pathParams.add("vaultId", "vault3000");

		Mockito.when(context.getHeaderString("Client-Jwt")).thenReturn(VALID_TOKEN_VAULT_2);
		Mockito.when(context.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(context.getUriInfo().getPathParameters()).thenReturn(pathParams);

		Assertions.assertThrows(VaultOwnerValidationFailedException.class, () -> vaultOwnerOnlyFilterProvider.filter(context));
	}

	@Test
	@DisplayName("validate expired Client-Jwt header")
	public void testExpiredClientJwtHeader() {
		var pathParams = new MultivaluedHashMap<String, String>();
		pathParams.add("vaultId", "vault2");

		Mockito.when(context.getHeaderString("Client-Jwt")).thenReturn(EXPIRED_TOKEN);
		Mockito.when(context.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(context.getUriInfo().getPathParameters()).thenReturn(pathParams);

		Assertions.assertThrows(VaultOwnerTokenExpiredException.class, () -> vaultOwnerOnlyFilterProvider.filter(context));
	}

	@Test
	@DisplayName("validate Client-Jwt header signed by other key")
	public void testOtherKeyClientJwtHeader() {
		var pathParams = new MultivaluedHashMap<String, String>();
		pathParams.add("vaultId", "vault2");

		Mockito.when(context.getHeaderString("Client-Jwt")).thenReturn(TOKEN_WITH_INVALID_SIGNATURE);
		Mockito.when(context.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(context.getUriInfo().getPathParameters()).thenReturn(pathParams);

		Assertions.assertThrows(VaultOwnerValidationFailedException.class, () -> vaultOwnerOnlyFilterProvider.filter(context));
	}

	@Test
	@DisplayName("validate malformed Client-Jwt header")
	public void testMalformedClientJwtHeader() {
		var pathParams = new MultivaluedHashMap<String, String>();
		pathParams.add("vaultId", "vault2");

		Mockito.when(context.getHeaderString("Client-Jwt")).thenReturn(MALFORMED_TOKEN);
		Mockito.when(context.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(context.getUriInfo().getPathParameters()).thenReturn(pathParams);

		Assertions.assertThrows(VaultOwnerValidationFailedException.class, () -> vaultOwnerOnlyFilterProvider.filter(context));
	}

	@Test
	@DisplayName("validate malformed key in database")
	public void testMalformedKeyInDatabase() throws SQLException {
		var pathParams = new MultivaluedHashMap<String, String>();
		pathParams.add("vaultId", "vault3000");

		Mockito.when(context.getHeaderString("Client-Jwt")).thenReturn(VALID_TOKEN_VAULT_3000);
		Mockito.when(context.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(context.getUriInfo().getPathParameters()).thenReturn(pathParams);

		try (var s = dataSource.getConnection().createStatement()) {
			s.execute("""
					INSERT INTO "vault" ("id", "name", "description", "creation_time", "salt", "iterations", "masterkey", "auth_pubkey", "auth_prvkey") 
					VALUES ('vault3000', 'Vault 1000', 'This is a testvault.', '2020-02-20 20:20:20', 'salt3000', 'iterations3000', 'masterkey3000', 'pubkey', 'prvkey'),
					""");

			Assertions.assertThrows(VaultOwnerValidationFailedException.class, () -> vaultOwnerOnlyFilterProvider.filter(context));
		}
	}

	@Test
	@DisplayName("validate valid vaultId in query")
	public void testGetValidVaultIdQueryParameter() {
		var pathParams = new MultivaluedHashMap<String, String>();
		pathParams.add("vaultId", "vault2");

		Mockito.when(context.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(context.getUriInfo().getPathParameters()).thenReturn(pathParams);

		String result = vaultOwnerOnlyFilterProvider.getVaultIdQueryParameter(context);
		Assertions.assertEquals("vault2", result);
	}

	@Test
	@DisplayName("validate no vaultId in query")
	public void testNoVaultIdQueryParameter() {
		var pathParams = new MultivaluedHashMap<String, String>();

		Mockito.when(context.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(context.getUriInfo().getPathParameters()).thenReturn(pathParams);

		Assertions.assertThrows(VaultOwnerValidationFailedException.class, () -> vaultOwnerOnlyFilterProvider.getVaultIdQueryParameter(context));
	}

	@Test
	@DisplayName("validate multiple vaultId in query")
	public void testMultipleVaultIdQueryParameter() {
		var pathParams = new MultivaluedHashMap<String, String>();
		pathParams.add("vaultId", "vault2");
		pathParams.add("vaultId", "vault3");

		Mockito.when(context.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(context.getUriInfo().getPathParameters()).thenReturn(pathParams);

		Assertions.assertThrows(VaultOwnerValidationFailedException.class, () -> vaultOwnerOnlyFilterProvider.getVaultIdQueryParameter(context));
	}

	@Test
	@DisplayName("validate valid Client-Jwt")
	public void testValidClientJwtProvided() {
		Mockito.when(context.getHeaderString("Client-Jwt")).thenReturn(VALID_TOKEN_VAULT_2);

		String result = vaultOwnerOnlyFilterProvider.getClientJwt(context);
		Assertions.assertEquals(VALID_TOKEN_VAULT_2, result);
	}

	@Test
	@DisplayName("validate no Client-Jwt")
	public void testNoClientJwtProvided() {
		Assertions.assertThrows(VaultOwnerNotProvidedException.class, () -> vaultOwnerOnlyFilterProvider.getClientJwt(context));
	}

	@Test
	@DisplayName("validate valid Client-Jwt leads to valid vaultId")
	public void testValidClientJwtLeadsToValidVaultId() {
		String result = vaultOwnerOnlyFilterProvider.getUnverifiedVaultId(VALID_TOKEN_VAULT_2);
		Assertions.assertEquals("vault2", result);
	}

	@Test
	@DisplayName("validate no Client-Jwt")
	public void testMalformedClientJwt() {
		Assertions.assertThrows(VaultOwnerValidationFailedException.class, () -> vaultOwnerOnlyFilterProvider.getUnverifiedVaultId(MALFORMED_TOKEN));
	}

	@Test
	@DisplayName("validate no vaultId in Client-Jwt")
	public void testNoVaultIdInJwt() {
		Assertions.assertThrows(VaultOwnerValidationFailedException.class, () -> vaultOwnerOnlyFilterProvider.getUnverifiedVaultId(TOKEN_WITHOUT_VAULT_ID));
	}

	@Test
	@DisplayName("validate invalid vaultId in Client-Jwt")
	public void testInvalidVaultIdInJwt() {
		Assertions.assertThrows(VaultOwnerValidationFailedException.class, () -> vaultOwnerOnlyFilterProvider.getUnverifiedVaultId(TOKEN_WITH_INVALID_VAULT_ID));
	}

}