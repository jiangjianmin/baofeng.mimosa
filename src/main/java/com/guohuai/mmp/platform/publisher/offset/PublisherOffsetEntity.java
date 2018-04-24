package com.guohuai.mmp.platform.publisher.offset;

import java.io.Serializable;
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
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.sys.SysConstant;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-发行人-轧差
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_PUBLISHER_OFFSET")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PublisherOffsetEntity extends UUID implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -6188747033795582042L;
	
	public static final String OFFSET_clearStatus_toClear = "toClear";
	public static final String OFFSET_clearStatus_clearing = "clearing";
	public static final String OFFSET_clearStatus_cleared = "cleared";
	
	public static final String OFFSET_confirmStatus_toConfirm = "toConfirm";
	public static final String OFFSET_confirmStatus_confirming = "confirming";
	public static final String OFFSET_confirmStatus_confirmed = "confirmed";
	public static final String OFFSET_confirmStatus_confirmFailed = "confirmFailed";

	
	//'toClose:待结算，closing:结算中，closed:已结算，closeSubmitFailed:结算申请失败，closePayFailed:结算支付失败'
	public static final String OFFSET_closeStatus_toClose = "toClose";
	public static final String OFFSET_closeStatus_closing = "closing";
	public static final String OFFSET_closeStatus_closed = "closed";
	public static final String OFFSET_closeStatus_closeSubmitFailed = "closeSubmitFailed";
	public static final String OFFSET_closeStatus_closePayFailed = "closePayFailed";
	
	//closeMan             varchar(32) comment 'platform:平台，publisher:发行人',
	public static final String OFFSET_closeMan_platform = "platform";
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
	 * 所属发行人
	 */
	@JoinColumn(name = "publisherOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	private PublisherBaseAccountEntity publisherBaseAccount;

	/**
	 * 轧差批次
	 */
	private String offsetCode;

	/**
	 * 轧差日期
	 */
	private Date offsetDate;

	/**
	 * 申购金额
	 */
	private BigDecimal investAmount = SysConstant.BIGDECIMAL_defaultValue;



	/**
	 * 赎回金额
	 */
	private BigDecimal redeemAmount = SysConstant.BIGDECIMAL_defaultValue;

	/**
	 * 净头寸
	 */
	private BigDecimal netPosition = SysConstant.BIGDECIMAL_defaultValue;
	

	/**
	 * 待结算赎回订单笔数
	 */
	private Integer toCloseRedeemAmount = SysConstant.INTEGER_defaultValue;
	
	/**
	 * 清算状态
	 */
	private String clearStatus;
	/**
	 * 交收状态
	 */
	private String confirmStatus;
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
