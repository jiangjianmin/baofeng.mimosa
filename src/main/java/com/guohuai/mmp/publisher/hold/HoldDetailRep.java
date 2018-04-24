package com.guohuai.mmp.publisher.hold;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class HoldDetailRep extends BaseRep {

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
	 * 持仓总份额
	 */
	private BigDecimal totalHoldVolume;

	/**
	 * 可计息份额
	 */
	private BigDecimal accruableHoldVolume;
	/**
	 * 计息锁定份额
	 */
	private BigDecimal accrueLockHoldVolume;
	/**
	 * 可赎回份额
	 */
	private BigDecimal redeemableHoldVolume;
	/**
	 * 赎回锁定份额
	 */
	private BigDecimal lockRedeemHoldVolume;

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
	private BigDecimal expectIncome;

	/**
	 * 份额确认日期
	 */
	private Date lastConfirmDate;

	/**
	 * 开仓时间
	 */
	private Timestamp openTime;

	/**
	 * 持仓状态
	 */
	private String holdStatus;
	private String holdStatusDisp;

	// 资产池剩余可募集规模
	private BigDecimal poolOverplusVolume;
}
