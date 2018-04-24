package com.guohuai.mmp.platform.inform;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-系统通知
 * 
 * @author yuechao
 *
 */

@Entity
@Table(name = "T_MONEY_PLATFORM_INFORM")
@lombok.Builder
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InformEntity extends UUID {
	/**
	* 
	*/
	private static final long serialVersionUID = 8740641012117327142L;
	
	
	/** 通知类型--产品通过 */
	public static final String INFORM_informType_productPass = "productPass";
	/** 通知类型--产品通过 */
	public static final String INFORM_informType_productRefused = "productRefused";
	/** 通知类型--产品通过 */
	public static final String INFORM_informType_publisherCloseCollect = "publisherCloseCollect";
	/** 通知类型--产品通过 */
	public static final String INFORM_informType_publisherCloseExpired = "publisherCloseExpired";
	
	/**
	 * 通知编号
	 */
	String informCode;
	/**
	 * 通知类型
	 */
	String informType;
	/**
	 * 通知内容
	 */
	String informContent;
	Timestamp updateTime;
	Timestamp createTime;
}
