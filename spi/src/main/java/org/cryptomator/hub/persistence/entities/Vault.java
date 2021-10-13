package org.cryptomator.hub.persistence.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "vault")
public class Vault extends PanacheEntityBase {

	@Id
	@Column(name = "id", nullable = false)
	public String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", updatable = false, nullable = false)
	public User owner;

	@OneToMany(mappedBy = "device", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.LAZY)
	public Set<Access> access = new HashSet<>();

	@Column(name = "name", nullable = false)
	public String name;

	@Column(name = "salt", nullable = false)
	public String salt;

	@Column(name = "iterations", nullable = false)
	public String iterations;

	@Column(name = "masterkey", nullable = false)
	public String masterkey;

	public void setAccess(Set<Access> access) {
		this.access.clear();
		this.access.addAll(access);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vault vault = (Vault) o;
		return Objects.equals(id, vault.id)
				&& Objects.equals(owner, vault.owner)
				&& Objects.equals(access, vault.access)
				&& Objects.equals(name, vault.name)
				&& Objects.equals(salt, vault.salt)
				&& Objects.equals(iterations, vault.iterations)
				&& Objects.equals(masterkey, vault.masterkey);
	}

    /*@Override
    public int hashCode() {
        return Objects.hash(id, user, access, name, salt, costParam, masterkey);
    }*/

	@Override
	public String toString() {
		return "Vault{" +
				"id='" + id + '\'' +
				", user=" + owner +
				", access=" + access.stream().map(Access::getId).collect(Collectors.toList()) +
				", name='" + name + '\'' +
				", salt='" + salt + '\'' +
				", iterations='" + iterations + '\'' +
				", masterkey='" + masterkey + '\'' +
				'}';
	}
}
