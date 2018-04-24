package com.guohuai.mmp.investor.baseaccount.changephone;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import org.hibernate.annotations.DynamicInsert;

import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_MONEY_BASEACCOUNT_PHONE_CHANGELOG")
@EqualsAndHashCode(callSuper = false)
@DynamicInsert
@DynamicUpdate
public class ChangePhoneEntity extends UUID implements Serializable{

	private static final long serialVersionUID = 7049852535259033506L;
	
	private String investorOid;
	
	private String oldPhone;
	
	private String newPhone;
	
	private Timestamp createTime;
	
	private int status;

}
