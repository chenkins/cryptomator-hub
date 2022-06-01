package org.cryptomator.hub.entities;

import com.radcortez.flyway.test.annotation.DataSource;
import com.radcortez.flyway.test.annotation.FlywayTest;
import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.persistence.PersistenceException;
import javax.transaction.Transactional;

@QuarkusTest
@FlywayTest(value = @DataSource(url = "jdbc:h2:mem:test"), additionalLocations = {"classpath:org/cryptomator/hub/flyway"})
@DisplayName("Persistent Entities")
public class EntityIntegrationTest {

	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public static void tx(Runnable validation) {
		validation.run();
	}

	@Test
	@DisplayName("Removing a Device cascades to Access")
	public void removingDeviceCascadesToAccess() {
		tx(() -> {
			Device device = Device.findById("device1");
			/* FIXME Affects the deletion of the device such that the following transaction fails but should be executed.
			boolean match = Access.<Access>findAll().stream().anyMatch(a -> "device1".equals(a.device.id));
			Assertions.assertTrue(match);
			*/
			device.delete();
		});

		tx(() -> {
			var match = AccessToken.<AccessToken>findAll().stream().anyMatch(a -> "device1".equals(a.device.id));
			Assertions.assertFalse(match);
		});
	}

	@Test
	@DisplayName("User's device names need to be unique")
	public void testAddNonUniqueDeviceName() {
		tx(() -> {
			Device existingDevice = Device.findById("device1");
			Device conflictingDevice = new Device();
			conflictingDevice.id = "deviceX";
			conflictingDevice.name = existingDevice.name;
			conflictingDevice.owner = existingDevice.owner;
			conflictingDevice.publickey = "XYZ";

			PersistenceException thrown = Assertions.assertThrows(PersistenceException.class, conflictingDevice::persistAndFlush);
			Assertions.assertInstanceOf(ConstraintViolationException.class, thrown.getCause());
		});
	}

}