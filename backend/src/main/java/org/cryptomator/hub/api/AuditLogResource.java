package org.cryptomator.hub.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.cryptomator.hub.entities.AuditEvent;
import org.cryptomator.hub.entities.CreateVaultEvent;
import org.cryptomator.hub.entities.UnlockVaultEvent;
import org.cryptomator.hub.entities.UpdateVaultMembershipEvent;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

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
	@Parameter(name = "startDate", description = "the start date of the period as ISO 8601 datetime string, inclusive", in = ParameterIn.QUERY)
	@Parameter(name = "endDate", description = "the end date of the period as ISO 8601 datetime string, exclusive", in = ParameterIn.QUERY)
	@Parameter(name = "paginationId", description = "The smallest (asc ordering) or highest (desc ordering) audit entry id, not included in results. Used for pagination. ", in = ParameterIn.QUERY)
	@Parameter(name = "order", description = "The order of the queried table. Determines if most recent (desc) or oldest entries (asc) are considered first. Allowed Values are 'desc' (default) or 'asc'. Used for pagination.", in = ParameterIn.QUERY)
	@Parameter(name = "pageSize", description = "the maximum number of entries to return. Must be between 1 and 100.", in = ParameterIn.QUERY)
	@APIResponse(responseCode = "200", description = "Body contains list of events in the specified time interval")
	@APIResponse(responseCode = "400", description = "startDate or endDate not specified, startDate > endDate, order specified and not in ['asc','desc'] or pageSize not in [1 .. 100]")
	@APIResponse(responseCode = "403", description = "requesting user is does not have admin role")
	public EventList getAllEvents(@QueryParam("startDate") Instant startDate, @QueryParam("endDate") Instant endDate, @QueryParam("paginationId") Long paginationId, @QueryParam("order") @DefaultValue("desc") String order, @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
		if (startDate == null || endDate == null) {
			throw new BadRequestException("startDate and endDate must be specified");
		} else if (startDate.isAfter(endDate)) {
			throw new BadRequestException("startDate must be before endDate");
		} else if (!(order.equals("desc") || order.equals("asc"))) {
			throw new BadRequestException("order must be either 'asc' or 'desc'");
		} else if (pageSize < 1 || pageSize > 100) {
			throw new BadRequestException("pageSize must be between 1 and 100");
		} else if (paginationId == null) {
			throw new BadRequestException("paginationId must be specified");
		}

		var events = AuditEvent.findAllInPeriod(startDate, endDate, paginationId, order.equals("asc"), pageSize).map(AuditEventDto::fromEntity).toList();
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
			@JsonSubTypes.Type(value = CreateVaultEventDto.class, name = CreateVaultEvent.TYPE),
			@JsonSubTypes.Type(value = UnlockVaultEventDto.class, name = UnlockVaultEvent.TYPE),
			@JsonSubTypes.Type(value = UpdateVaultMembershipEventDto.class, name = UpdateVaultMembershipEvent.TYPE)
	})
	public interface AuditEventDto {

		@JsonProperty("id")
		long id();

		@JsonProperty("timestamp")
		Instant timestamp();

		static AuditEventDto fromEntity(AuditEvent entity) {
			if (entity instanceof CreateVaultEvent cve) {
				return new CreateVaultEventDto(cve.id, cve.timestamp, CreateVaultEvent.TYPE, cve.userId, cve.vaultId);
			} else if (entity instanceof UnlockVaultEvent uve) {
				return new UnlockVaultEventDto(uve.id, uve.timestamp, UnlockVaultEvent.TYPE, uve.userId, uve.vaultId, uve.deviceId, uve.result);
			} else if (entity instanceof UpdateVaultMembershipEvent uvme) {
				return new UpdateVaultMembershipEventDto(uvme.id, uvme.timestamp, UpdateVaultMembershipEvent.TYPE, uvme.userId, uvme.vaultId, uvme.authorityId, uvme.operation);
			} else {
				throw new UnsupportedOperationException("conversion not implemented for event type " + entity.getClass());
			}
		}
	}

	record CreateVaultEventDto(long id, Instant timestamp, String type, @JsonProperty("userId") String userId, @JsonProperty("vaultId") UUID vaultId) implements AuditEventDto {
	}

	record UnlockVaultEventDto(long id, Instant timestamp, String type, @JsonProperty("userId") String userId, @JsonProperty("vaultId") UUID vaultId, @JsonProperty("deviceId") String deviceId,
							   @JsonProperty("result") UnlockVaultEvent.Result result) implements AuditEventDto {
	}

	record UpdateVaultMembershipEventDto(long id, Instant timestamp, String type, @JsonProperty("userId") String userId, @JsonProperty("vaultId") UUID vaultId, @JsonProperty("authorityId") String authorityId,
										 @JsonProperty("operation") UpdateVaultMembershipEvent.Operation operation) implements AuditEventDto {
	}
}
