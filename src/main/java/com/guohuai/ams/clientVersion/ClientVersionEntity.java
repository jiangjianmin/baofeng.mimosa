package com.guohuai.ams.clientVersion;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;

import com.guohuai.component.persist.UUID;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@Entity
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ClientVersionEntity extends UUID implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 客户端id
	 */
	private String clientId;
	/**
	 * 客户端类型（Android、IOS）
	 */
	private String clientType;
	/**
	 * 版本
	 */
	private String version;
	/**
	 * 创建时间
	 */
	private Timestamp createTime;
}
