package com.guohuai.mmp.platform.investor.offset;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountEntity;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-投资人-轧差
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_INVESTOR_OFFSET")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class InvestorOffsetEntity extends UUID {
	/**
	* 
	*/
	private static final long serialVersionUID = -5938127661411628158L;
	/** 清算状态--待清算 */
	public static final String OFFSET_clearStatus_toClear = "toClear";
	/** 清算状态--清算中 */
	public static final String OFFSET_clearStatus_clearing = "clearing";
	/** 清算状态--已清算 */
	public static final String OFFSET_clearStatus_cleared = "cleared";
	/** 结算状态--待结算 */
	public static final String OFFSET_closeStatus_toClose = "toClose";
	/** 结算状态--结算中 */
	public static final String OFFSET_closeStatus_closing = "closing";
	/** 结算状态--结算申请失败 */
	public static final String OFFSET_closeStatus_closeSubmitFailed = "closeSubmitFailed";
	/** 结算状态--结算支付失败 */
	public static final String OFFSET_closeStatus_closePayFailed = "closePayFailed";
	/** 结算状态--已结算 */
	public static final String OFFSET_closeStatus_closed = "closed";
	
	
	/** 轧差频率--快速 */
	public static final String OFFSET_offsetFrequency_fast = "fast";
	/** 轧差频率--普通 */
	public static final String OFFSET_offsetFrequency_normal = "normal";
	
	/** 结算人--平台 */
	public static final String OFFSET_closeMan_platform = "platform";
	/** 结算人--发行人 */
	public static final String OFFSET_closeMan_publisher = "publisher";
	
	/** 逾期状态--已逾期 */
	public static final String OFFSET_overdueStatus_yes = "yes";
	/** 逾期状态--未逾期 */
	public static final String OFFSET_overdueStatus_no = "no";
	
	/**
	 * 所属平台
	 */
	@JoinColumn(name = "platformOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	private PlatformBaseAccountEntity platformBaseAccount;

	/**
	 * 轧差批次
	 */
	private String offsetCode;

	/**
	 * 轧差日期
	 */
	private Date offsetDate;
	
	/**
	 * 轧差频率
	 */
	private String offsetFrequency;

	/**
	 * 赎回金额
	 */
	private BigDecimal redeemAmount = SysConstant.BIGDECIMAL_defaultValue;
	
	/**
	 * 待赎回笔数
	 */
	private int toCloseRedeemAmount = SysConstant.INTEGER_defaultValue;

	/**
	 * 清算状态
	 */
	private String clearStatus;
	
	/**
	 * 结算状态
	 */
	private String closeStatus;

	/**
	 * 结算人
	 */
	private String closeMan;
	
	/**
	 * 逾期状态
	 */
	private String overdueStatus;

	private Timestamp updateTime;
	private Timestamp createTime;
}
