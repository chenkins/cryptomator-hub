package org.cryptomator.hub.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.cryptomator.hub.entities.AuditEvent;
import org.cryptomator.hub.entities.UnlockEvent;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/auditlog")
public class AuditLogResource {

	@GET
	@RolesAllowed("admin")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "list all auditlog entries within a period", description = "list all auditlog entries from a period specified by a start and end date")
	@Parameter(name = "startDate", description = "the start date of the period as ISO 8601 datetime string", in = ParameterIn.QUERY)
	@Parameter(name = "endDate", description = "the end date of the period as ISO 8601 datetime string", in = ParameterIn.QUERY)
	@Parameter(name = "after", description = "the id of the last entry of the previous page", in = ParameterIn.QUERY)
	@Parameter(name = "pageSize", description = "the maximum number of entries to return", in = ParameterIn.QUERY)
	public EventList getAllEvents(@QueryParam("startDate") Instant startDate, @QueryParam("endDate") Instant endDate, @QueryParam("after") long after, @QueryParam("pageSize") int pageSize) {
		var events = AuditEvent.findAllInPeriod(startDate, endDate, after, pageSize).map(AuditEventDto::fromEntity).toList();
		return new EventList(events);
	}

	// Helper class to prevent type erasure for @JsonTypeInfo, see https://github.com/FasterXML/jackson-databind/issues/336
	public static class EventList extends ArrayList<AuditEventDto> {
		EventList(List<AuditEventDto> events) {
			super(events);
		}
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonSubTypes({
			@JsonSubTypes.Type(value = UnlockEventDto.class, name = UnlockEvent.TYPE)
	})
	public interface AuditEventDto {

		@JsonProperty("id")
		long id();

		@JsonProperty("timestamp")
		Instant timestamp();

		static AuditEventDto fromEntity(AuditEvent entity) {
			if (entity instanceof UnlockEvent e) {
				return new UnlockEventDto(e.id, e.timestamp, UnlockEvent.TYPE, e.userId, e.vaultId, e.deviceId, e.result);
			} else {
				throw new UnsupportedOperationException("conversion not implemented for event type " + entity.getClass());
			}
		}
	}

	record UnlockEventDto(long id, Instant timestamp, String type, @JsonProperty("userId") String userId, @JsonProperty("vaultId") UUID vaultId, @JsonProperty("deviceId") String deviceId, @JsonProperty("result") UnlockEvent.Result result) implements AuditEventDto {
	}

}
