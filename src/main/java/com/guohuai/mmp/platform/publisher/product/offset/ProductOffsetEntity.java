package com.guohuai.mmp.platform.publisher.product.offset;

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

import com.guohuai.ams.product.Product;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetEntity;
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
@Table(name = "T_MONEY_PLATFORM_PUBLISHER_PRODUCT_OFFSET")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ProductOffsetEntity extends UUID {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1224610044930000868L;
	
	
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
	
	/**
	 * 所属发行人
	 */
	@JoinColumn(name = "publisherOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	private PublisherBaseAccountEntity publisherBaseAccount;
	
	/**
	 * 所属发行人轧差
	 */
	@JoinColumn(name = "offsetOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	private PublisherOffsetEntity publisherOffset;

	/**
	 * 所属产品
	 */
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	private Product product;

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
	

	private Timestamp updateTime;
	private Timestamp createTime;
}
