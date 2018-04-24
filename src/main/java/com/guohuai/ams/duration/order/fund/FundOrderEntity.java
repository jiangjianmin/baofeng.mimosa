package com.guohuai.ams.duration.order.fund;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.util.BigDecimalUtil;

import lombok.Data;

@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL_CASHTOOL_ORDER")
@DynamicInsert
@DynamicUpdate
public class FundOrderEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 状态
	 */
	public static final String STATE_FAIL			= "-2";
	public static final String STATE_AUDIT			= "-1";
	public static final String STATE_APPOINTMENT	= "0";
	public static final String STATE_CONFIRM		= "1";
	public static final String STATE_SUCCESS 		= "2";
	
	public FundOrderEntity() {
		this.volume 		= BigDecimalUtil.init0;
		this.returnVolume 	= BigDecimalUtil.init0;
		this.auditVolume 	= BigDecimalUtil.init0;
		this.reserveVolume 	= BigDecimalUtil.init0;
		this.investVolume 	= BigDecimalUtil.init0;
	}

	@Id
	private String oid;
	// 关联产品现金管理工具
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assetPoolCashtoolOid", referencedColumnName = "oid")
	private FundEntity fundEntity;
	// 类型(purchase;redeem)
	private String optType;
	// 投资日
	private Date investDate; 
	// 投资时间
	private Timestamp investTime;
	// 起息日
	private Date incomeDate; 
	// 申购额度
	private BigDecimal volume;
	// 发起赎回日
	private Date redeemDate; 
	// 资金到账日
	private Date backDate; 
	// 收益截止日
	private Date endYield;
	// 赎回份额
	private BigDecimal returnVolume;
	// 是否全部赎回
	private String allFlag;
	// 申请人
	private String asker;
	// 审核人
	private String auditor; 
	// 预约人
	private String reserver; 
	// 确认人
	private String confirmer; 
	// 审核额度
	private BigDecimal auditVolume;
	// 预约额度
	private BigDecimal reserveVolume;
	// 确认额度
	private BigDecimal investVolume;
	// 确认时间
	private Date confirmDate;
	// 状态（-2：失败，-1：待审核，0：待预约，1：待确认，2：已成立）
	private String state;
	// 操作员
	private String operator;
	// UpdateTime
	private Timestamp updateTime;
	// CreateTime
	private Timestamp createTime;
}
