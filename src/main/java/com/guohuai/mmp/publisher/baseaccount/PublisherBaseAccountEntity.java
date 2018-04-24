package com.guohuai.mmp.publisher.baseaccount;

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
 * 发行人-基本账户
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PUBLISHER_BASEACCOUNT")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PublisherBaseAccountEntity extends UUID {

	private static final long serialVersionUID = -6301241499172524045L;

	/** 状态--正常 */
	public static final String PUBLISHER_BASE_ACCOUNT_STATUS_normal = "normal";
	/** 状态--禁用 */
	public static final String PUBLISHER_BASE_ACCOUNT_STATUS_forbiden = "forbiden";

	/**
	 * 企业账号
	 */
	private String corperateOid;
	
	/**
	 * 三方会员账户
	 */
	private String memberId;
	
	/**
	 * 银行编号
	 */
	private String bankCode;

	/**
	 * 账户余额
	 */
	private BigDecimal accountBalance = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 状态
	 */
	private String status;

	private Timestamp updateTime;

	private Timestamp createTime;
}
