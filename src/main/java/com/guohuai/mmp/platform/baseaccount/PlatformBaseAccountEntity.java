package com.guohuai.mmp.platform.baseaccount;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-基本账户
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_BASEACCOUNT")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PlatformBaseAccountEntity extends UUID {
	/**
	* 
	*/
	private static final long serialVersionUID = -8265125748135226893L;
	
	/** 状态--正常 */
	public static final String PLATFORMBASE_accountStatus_normal = "normal";
	/** 状态--禁用 */
	public static final String PLATFORMBASE_accountStatus_forbiden = "forbiden";
	
	/**
	 * 平台账号
	 */
	private String platformUid;
	
	/**
	 * 超级户借款金额
	 */
	private BigDecimal superAccBorrowAmount = BigDecimal.ZERO;
	
	/**
	 * 余额
	 */
	private BigDecimal balance = BigDecimal.ZERO;
	/**
	 * 状态
	 */
	private String status;
	private Timestamp updateTime;
	private Timestamp createTime;
	
}
