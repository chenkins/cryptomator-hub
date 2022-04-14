package org.cryptomator.hub.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Inheritance
@Entity
@Table(name = "group_details")
@DiscriminatorValue("GROUP")
public class Group extends Authority {

	public static final String TYPE = "group";

	@ManyToMany
	@JoinTable(name = "group_membership",
			joinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "member_id", referencedColumnName = "id")
	)
	public Set<Authority> members = new HashSet<>();

}
