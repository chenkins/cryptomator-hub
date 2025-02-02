package org.cryptomator.hub.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Entity
@Table(name = "device")
@NamedQuery(name = "Device.requiringAccessGrant",
		query = """
				SELECT d
				FROM Vault v
					INNER JOIN v.effectiveMembers m
					INNER JOIN m.devices d
					LEFT JOIN d.accessTokens a ON a.id.vaultId = :vaultId AND a.id.deviceId = d.id
					WHERE v.id = :vaultId AND a.vault IS NULL
				"""
)
@NamedQuery(name = "Device.allInList",
		query = """
				SELECT d
				FROM Device d
				WHERE d.id IN :ids
				""")
public class Device extends PanacheEntityBase {

	public enum Type {
		BROWSER, DESKTOP, MOBILE
	}

	@Id
	@Column(name = "id", nullable = false)
	public String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id", updatable = false, nullable = false)
	public Authority owner;

	@OneToMany(mappedBy = "device", fetch = FetchType.LAZY)
	public Set<AccessToken> accessTokens = new HashSet<>();

	@Column(name = "name", nullable = false)
	public String name;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	public Type type;

	@Column(name = "publickey", nullable = false)
	public String publickey;

	@Column(name = "creation_time", nullable = false)
	public Instant creationTime;

	@Override
	public String toString() {
		return "Device{" +
				"id='" + id + '\'' +
				", owner=" + owner.id +
				", name='" + name + '\'' +
				", type='" + type + '\'' +
				", publickey='" + publickey + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Device other = (Device) o;
		return Objects.equals(this.id, other.id)
				&& Objects.equals(this.owner, other.owner)
				&& Objects.equals(this.name, other.name)
				&& Objects.equals(this.type, other.type)
				&& Objects.equals(this.publickey, other.publickey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, owner, name, type, publickey);
	}

	public static Stream<Device> findRequiringAccessGrant(UUID vaultId) {
		return find("#Device.requiringAccessGrant", Parameters.with("vaultId", vaultId)).stream();
	}

	public static Stream<Device> findAllInList(List<String> ids) {
		return find("#Device.allInList", Parameters.with("ids", ids)).stream();
	}
}
