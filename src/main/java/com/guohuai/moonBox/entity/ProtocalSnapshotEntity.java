package com.guohuai.moonBox.entity;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 家庭理财计划协议快照表
 * 
 * @author jiangjianmin
 *
 */

@Entity
@Table(name = "T_MONEY_PROTOCOL_SNAPSHOT")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ProtocalSnapshotEntity extends UUID {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4333179226422640561L;
	/**
	 * oid
	 */
//	private String oid;

	/**
	 * 投资人id
	 */
	private String investOid; 
	/**
	 * 产品oid
	 */
	private String productOid;
	/**
	 * 协议id
	 */
	private String protocalOid;
	/**
	 * 协议名称
	 */
	private String protocalName;

	/**
	 * 协议金额
	 */
	private BigDecimal protocalAmount = SysConstant.BIGDECIMAL_defaultValue;
	
}
