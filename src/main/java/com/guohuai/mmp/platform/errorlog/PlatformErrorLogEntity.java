package com.guohuai.mmp.platform.errorlog;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "T_MONEY_PLATFORM_ERRORLOG")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PlatformErrorLogEntity extends UUID {


	
	/**
	 * 
	 */
	private static final long serialVersionUID = -225373685774845242L;
	private String uid;
	private String reqUri;
	private String params;

	private Timestamp createTime;

	
}
