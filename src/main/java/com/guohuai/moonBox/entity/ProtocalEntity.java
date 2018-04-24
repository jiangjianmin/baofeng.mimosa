package com.guohuai.moonBox.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 家庭理财计划协议表
 * 
 * @author jiangjianmin
 *
 */
@Entity
@Table(name = "T_MONEY_PROTOCOL")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ProtocalEntity extends UUID {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4333179226422640561L;
	/**
	 * 投资人id
	 */
	public String investOid; 
	/**
	 * 产品oid
	 */
	public String productOid;

	/**
	 * 协议名称
	 */
	public String protocalName;

	/**
	 * 协议金额
	 */
	public BigDecimal protocalAmount = SysConstant.BIGDECIMAL_defaultValue;
	/**
	 * 每月转入日期
	 */
	public String protocalDate;
	/**
	 * 协议状态 0开通 1作废
	 */
	public String protocalStatus;
	/**
	 * 下月转日日期
	 */
	public String nextInvestDate;
   
	/**
	 * 最后一次转入成功日期
	 */
	public Timestamp LastPayDate;

	/**
	 * 协议标签
	 */
	public String protocalLabel;

	public Timestamp createTime;

	public Timestamp updateTime;
	
}
