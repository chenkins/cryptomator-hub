package org.cryptomator.hub.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "grant_vault_access_event")
@DiscriminatorValue(GrantVaultAccessEvent.TYPE)
public class GrantVaultAccessEvent extends AuditEvent {

	public static final String TYPE = "GRANT_VAULT_ACCESS";

	@Column(name = "granted_by")
	public String grantedBy;

	@Column(name = "vault_id")
	public UUID vaultId;

	@Column(name = "authority_id")
	public String authorityId;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GrantVaultAccessEvent that = (GrantVaultAccessEvent) o;
		return super.equals(that) //
				&& Objects.equals(grantedBy, that.grantedBy) //
				&& Objects.equals(vaultId, that.vaultId) //
				&& Objects.equals(authorityId, that.authorityId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, grantedBy, vaultId, authorityId);
	}

	public static void log(String grantedBy, UUID vaultId, String authorityId) {
		var event = new GrantVaultAccessEvent();
		event.timestamp = Instant.now();
		event.grantedBy = grantedBy;
		event.vaultId = vaultId;
		event.authorityId = authorityId;
		event.persist();
	}

}
