package com.guohuai.mmp.publisher.hold;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;


import lombok.Data;

@Data
public class HoldQueryRep implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String investorOid;
	/**
	 * 投资人手机号
	 */
	private String phoneNum;
	
	/**
	 * 持仓OID
	 */
	private String holdOid;
	
	/**
	 * 产品OID
	 */
	private String productOid;
	
	/**
	 * 产品编号
	 */
	private String productCode;
	/**
	 * 产品名称
	 */
	private String productName;
	/**
	 * 预期年化收益率
	 */
	private String expAror;
	/**
	 * 锁定期
	 */
	private int lockPeriod;
	/**
	 * 总份额
	 */
	private BigDecimal totalVolume;
	private BigDecimal totalAmount;
	
	/**
	 * 持有份额
	 */
	private BigDecimal toConfirmRedeemVolume;
	private BigDecimal toConfirmRedeemAmount;
	/**
	 * 待确认投资份额
	 */
	private BigDecimal toConfirmInvestVolume;
	private BigDecimal toConfirmInvestAmount;
	
	/**
	 * 可计息份额
	 */
	private BigDecimal accruableHoldVolume;
	private BigDecimal accruableHoldAmount;
	
	/**
	 * 可赎回份额
	 */
	private BigDecimal redeemableHoldVolume;
	private BigDecimal redeemableHoldAmount;
	
	/**
	 * 赎回锁定份额
	 */
	private BigDecimal lockRedeemHoldVolume;
	private BigDecimal lockRedeemHoldAmount;
	
	/**
	 * 最新市值
	 */
	private BigDecimal value;
	
	/**
	 * 累计收益
	 */
	private BigDecimal holdTotalIncome;
	/**
	 * 昨日收益
	 */
	private BigDecimal holdYesterdayIncome;
	
	/**
	 * 待结转收益
	 */
	private BigDecimal toConfirmIncome;
	/**
	 * 总收益
	 */
	private BigDecimal incomeAmount;
	
	/**
	 * 可赎回收益
	 */
	private BigDecimal redeemableIncome;
	/**
	 * 锁定收益
	 */
	private BigDecimal lockIncome;
	/**
	 * 预期收益
	 */
	private String expectIncome;
	
	/**
	 * 份额确认日期
	 */
	private Date confirmDate;
	
	
	/**
	 * 持仓状态
	 */
	private String holdStatus;
	private String holdStatusDisp;
	
	/**
	 * spv 名称
	 */
	private String spvName;
	
	/**
	 * spv oid
	 */
	private String spvOid;
	
	/**
	 * 类型
	 */
	private String accountType;
	
	/**
	 * 累计基础收益
	 */
	private BigDecimal totalBaseIncome;
	
	/**
	 * 累计奖励收益
	 */
	private BigDecimal totalRewardIncome;
	
	
	/**
	 * 单个产品最大持有份额
	 */
	private BigDecimal maxHoldVolume;
	
	/**
	 * 
	 * 单日赎回份额
	 */
	private BigDecimal dayRedeemVolume;
	
	/**
	 * 
	 * 单日投资份额
	 */
	private BigDecimal dayInvestVolume;
	

}
