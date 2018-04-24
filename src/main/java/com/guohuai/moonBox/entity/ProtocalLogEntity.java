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
 * 家庭理财计划变更日志表
 * 
 * @author jiangjianmin
 *
 */
@Entity
@Table(name = "T_MONEY_PROTOCOL_LOG")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ProtocalLogEntity extends UUID {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4333179226422640561L;
	/**
	 * 投资人id
	 */
	public String investOid; 
	/**
	 * 协议id
	 */
	public String protocalOid;
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
	 * 操作状态 0新增 1修改 2删除
	 */
	public String operateStatus;
	/**
	 * 下月转日日期
	 */
	public String nextInvestDate;
   

	/**
	 * 协议标签
	 */
	public String protocalLabel;

	public Timestamp createTime;

	
}
