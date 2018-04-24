package com.guohuai.component.persist;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

//JPA 基类的标识
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class UUID implements Cloneable, Serializable {

//	@Id
//	@GenericGenerator(name = "uuid", strategy = "uuid.hex")
//	@GeneratedValue(strategy = GenerationType.AUTO, generator = "uuid")
	@Id
	@GeneratedValue(generator = "assignOrUUIDGenerator")
	@GenericGenerator(name = "assignOrUUIDGenerator", strategy = "com.guohuai.component.persist.AssignOrUUIDGenerator")
	protected String oid;

	@Transient
	protected  String assignedId;

	public String getAssignedId() {
		return assignedId;
	}

	public void setAssignedId(String assignedId) {
		this.assignedId = assignedId;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	@Override
	public int hashCode() {
		return null == this.oid ? "".hashCode() : this.oid.hashCode();
	}

	@Override
	public String toString() {
		return null == this.oid ? "" : this.oid;
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (!(obj instanceof UUID)) {
			return false;

		}
		UUID ref = (UUID) obj;
		return null == this.oid ? null == ref.getOid() : this.oid.equals(ref.getOid());

	}

}
