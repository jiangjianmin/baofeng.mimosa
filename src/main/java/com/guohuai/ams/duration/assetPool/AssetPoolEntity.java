package com.guohuai.ams.duration.assetPool;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.util.BigDecimalUtil;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;

import lombok.Data;

/**
 * 存续期--资产池对象
 * @author star.zhu
 * 2016年5月16日
 */
@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL")
@DynamicInsert
@DynamicUpdate
public class AssetPoolEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 状态
	 */
	public static final String status_create 	= "ASSETPOOLSTATE_01";
	public static final String status_duration 	= "ASSETPOOLSTATE_02";
	public static final String status_unpass 	= "ASSETPOOLSTATE_03";
	public static final String status_invalid 	= "ASSETPOOLSTATE_04";
	
	/**
	 * 每日定时任务状态(未计算，已计算，部分计算，当日不计算)
	 */
	public static final String schedule_wjs 	= "未计算";
	public static final String schedule_yjs 	= "已计算";
	public static final String schedule_bfjs 	= "部分计算";
	public static final String schedule_drbjs 	= "当日不计算";
	
	/**
	 * 当日收益分配状态(未分配，已分配)
	 */
	public static final String income_wfp 	= "未分配";
	public static final String income_yfp 	= "已分配";
	
	public AssetPoolEntity() {
		this.scale 				= BigDecimalUtil.init0;
		this.cashRate 			= BigDecimalUtil.init0;
		this.cashtoolRate 		= BigDecimalUtil.init0;
		this.targetRate 		= BigDecimalUtil.init0;
		this.cashPosition 		= BigDecimalUtil.init0;
		this.freezeCash 		= BigDecimalUtil.init0;
		this.freezeCashForFund 	= BigDecimalUtil.init0;
		this.transitCash 		= BigDecimalUtil.init0;
		this.confirmProfit 		= BigDecimalUtil.init0;
		this.factProfit 		= BigDecimalUtil.init0;
		this.cashtoolFactRate 	= BigDecimalUtil.init0;
		this.targetFactRate 	= BigDecimalUtil.init0;
		this.deviationValue 	= BigDecimalUtil.init0;
		this.marketValue 		= BigDecimalUtil.init0;
		this.shares 			= BigDecimalUtil.init0;
		this.nav 				= BigDecimalUtil.init0;
		this.countintChargefee 	= BigDecimalUtil.init0;
		this.drawedChargefee 	= BigDecimalUtil.init0;
//		this.factValuation 		= 0;
		this.calcBasis 			= 0;
		this.scheduleState 		= "初始化";
		this.incomeState 		= income_wfp;

		this.netValue 			= BigDecimalUtil.init0;
		this.trusteeRate 		= BigDecimalUtil.init0;
		this.trusteeFee 		= BigDecimalUtil.init0;
		this.manageRate 		= BigDecimalUtil.init0;
		this.manageFee 			= BigDecimalUtil.init0;
	}

	@Id
	private String oid;
	private String name;
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "spvOid", referencedColumnName = "oid")
//	private SPV spvEntity;
	private PublisherBaseAccountEntity spvEntity;
	// 资产规模(估值)
	private BigDecimal scale;
	// 真实市值
	private BigDecimal marketValue;
	// 现金比例
	private BigDecimal cashRate;
	// 货币基金（现金类管理工具）比例
	private BigDecimal cashtoolRate;
	// 信托（计划）比例
	private BigDecimal targetRate;
	// 现金实际比例
	private BigDecimal cashFactRate;
	// 货币基金（现金类管理工具）实际比例
	private BigDecimal cashtoolFactRate;
	// 信托（计划）实际比例
	private BigDecimal targetFactRate;
	// 可用现金
	private BigDecimal cashPosition;
	// 冻结资金
	private BigDecimal freezeCash;
	// 冻结资金（累计申购现金类管理工具）
	private BigDecimal freezeCashForFund;
	// 在途资金
	private BigDecimal transitCash;
	// 确认收益
	private BigDecimal confirmProfit;
	// 实现收益
	private BigDecimal factProfit;
	// 偏离损益
	private BigDecimal deviationValue;
	// 成立状态(未审核,存续期,未通过,已失效)
	private String state;
	// 每日定时任务状态(未计算，已计算，部分计算，当日不计算)
	private String scheduleState;
	// 当日收益分配状态(未分配，已分配)
	private String incomeState;
	// 是否录入真实估值(0:否;1:是)
//	private int factValuation;
	// 未分配收益
//	private BigDecimal unDistributeProfit;
	// 应付费金
//	private BigDecimal payFeigin;
	// spv所有者权益
//	private BigDecimal spvProfit;
	// 投资者所有权益
	private BigDecimal investorProfit;
	// 创建者
	private String creater;
	// 操作员
	private String operator;
	// 估值基准日
	private Date baseDate;
	// SPV持有的基子份额
	private BigDecimal shares;
	// SPV持有的基子单位净值
	private BigDecimal nav;
	//  SPV累计计提费金
	private BigDecimal countintChargefee;
	// SPV累计提取费金
	private BigDecimal drawedChargefee;
	// 资管机构名称
	private String organization;
	// 资管计划名
	private String planName;
	// 银行名称
	private String bank;
	// 银行账号
	private String account;
	// 联系人
	private String contact;
	// 联系电话
	private String telephone;
	// 基础资产编号
	private String baseAssetCode;
	// 费用计算基础
	private Integer calcBasis;
	// 创建日期
	private Timestamp createTime;
	// 更新日期
	private Timestamp updateTime;
	
	public static final Map<String, String> PoolState = new HashMap<>();
	static {
		PoolState.put("ASSETPOOLSTATE_01", "未审核");
		PoolState.put("ASSETPOOLSTATE_02", "存续期");
		PoolState.put("ASSETPOOLSTATE_03", "未通过");
		PoolState.put("ASSETPOOLSTATE_04", "已失效");
	}
	
	// 资产净值
	private BigDecimal netValue;
	// 托管费率
	private BigDecimal trusteeRate;
	// 应付托管费
	private BigDecimal trusteeFee;
	// 管理费率
	private BigDecimal manageRate;
	// 应付管理费
	private BigDecimal manageFee;
	// 非交易日收益
	private int nonTradingDays;
	// 单人累计申购上限
	private BigDecimal purchaseLimit;
}
