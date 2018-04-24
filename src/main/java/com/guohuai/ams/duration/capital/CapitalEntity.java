package com.guohuai.ams.duration.capital;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.util.BigDecimalUtil;

import lombok.Data;

/**
 * 存续期--账户对象
 * @author star.zhu
 * 2016年5月16日
 */
@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL_CASH_LOG")
@DynamicInsert
@DynamicUpdate
public class CapitalEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 订单状态
	 */
	public static final String APPLY00 			= "00";	// 申请待审核
	public static final String AUDIT10 			= "10";	// 审核未通过
	public static final String AUDIT11 			= "11";	// 审核通过待预约
	public static final String APPOINTMENT20 	= "20";	// 预约未通过
	public static final String APPOINTMENT21 	= "21";	// 预约通过待确认
	public static final String CONFIRM30 		= "30";	// 确认未通过
	public static final String CONFIRM31 		= "31";	// 确认通过
	
	/**
	 * 操作类型（现金管理工具申购，现金管理工赎回，投资标的申购，本息兑付，投资标的转入，投资标的转出）
	 */
	public static final String PURCHASE 		= "现金管理工具申购";	
	public static final String REDEEM 			= "现金管理工赎回";	
	public static final String APPLY 			= "投资标的申购";	
	public static final String INCOME 			= "本息兑付";		
	public static final String BACK 			= "退款";	
	public static final String TRANS 			= "投资标的转入";	
	public static final String TRANSFER 		= "投资标的转让";	
	public static final String OVERDUETRANSFER 	= "投资标的逾期转让";	
	
	
	public CapitalEntity() {
		this.freezeCash 	= BigDecimalUtil.init0;
		this.unfreezeCash 	= BigDecimalUtil.init0;
		this.transitCash 	= BigDecimalUtil.init0;
		this.untransitCash 	= BigDecimalUtil.init0;
		this.inputCash 		= BigDecimalUtil.init0;
		this.outputCash 	= BigDecimalUtil.init0;
	}
	
	@Id
	private String oid;
	// 关联资产池
	private String assetPoolOid;
	// 科目
	@Column(name = "logSubject")
	private String subject;
	// 关联投资标的申购订单
	private String targetOrderOid;
	// 关联投资标的本息兑付订单
	private String targetIncomeOid;
	// 关联投资标的资产转让订单
	private String targetTransOid;
	// 关联现金管理工具申赎订单
	private String cashtoolOrderOid;
	// 冻结资金入金
	private BigDecimal freezeCash;
	// 冻结资金出金
	private BigDecimal unfreezeCash;
	// 在途资金入金
	private BigDecimal transitCash;
	// 在途资金出金
	private BigDecimal untransitCash;
	// 资金变动入金
	private BigDecimal inputCash;
	// 资金变动出金
	private BigDecimal outputCash;
	// 操作类型（现金管理工具申购，现金管理工赎回，投资标的申购，本息兑付，投资标的转入，投资标的转出）
	private String operation;
	// 操作人
	private String operator;
	// 状态
	private String state;
	// CreateTime
	private Timestamp createTime;
	// UpdateTime
	private Timestamp updateTime;
}
