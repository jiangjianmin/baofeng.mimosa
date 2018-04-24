package com.guohuai.mmp.platform.baseaccount.statistics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.sys.SysConstant;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台首页
 * 
 * @author wanglei
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
public class PlatformHomeQueryRep extends BaseRep {

	/** 累计交易总额 */
	private BigDecimal totalTradeAmount = SysConstant.BIGDECIMAL_defaultValue;
	/** 平台累计投资金额(累计借款总额) */
	private BigDecimal totalInvestAmount = SysConstant.BIGDECIMAL_defaultValue;
	/** 累计注册人数 */
	private Integer registerAmount = SysConstant.INTEGER_defaultValue;
	/** 累计投资人数 */
	private Integer investorAmount = SysConstant.INTEGER_defaultValue;
	/** 投资人充值总额 */
	private BigDecimal investorTotalDepositAmount = SysConstant.BIGDECIMAL_defaultValue;
	/** 投资人提现总额 */
	private BigDecimal investorTotalWithdrawAmount = SysConstant.BIGDECIMAL_defaultValue;
	
	/** 累计还款总额 */
	private BigDecimal totalReturnAmount = SysConstant.BIGDECIMAL_defaultValue;
	/** 备付金余额(平台备付金账户-余额) */
	private BigDecimal balance = SysConstant.BIGDECIMAL_defaultValue;
	

	/** 企业用户数（发行人数） */
	private BigDecimal publisherAmount = SysConstant.BIGDECIMAL_defaultValue;
	
	/** 发行产品数 */
	private Integer productAmount = SysConstant.INTEGER_defaultValue;
	/** 已还产品数（已结算产品数） */
	private Integer closedProductAmount = SysConstant.INTEGER_defaultValue;
	/** 待还产品数 */
	private Integer toCloseProductAmount = SysConstant.INTEGER_defaultValue;
	/** 在售产品数 */
	private Integer onSaleProductAmount = SysConstant.INTEGER_defaultValue;

	/** 今日在线用户数(活跃投资人数) */
	private Integer todayOnline = SysConstant.INTEGER_defaultValue;
	/** 今日投资金额 */
	private BigDecimal todayInvestAmount = SysConstant.BIGDECIMAL_defaultValue;
	/** 今日注册用户数 */
	private Integer todayRegisterAmount = SysConstant.INTEGER_defaultValue;
	/** 今日实名认证用户数 */
	private Integer todayVerifiedInvestorAmount = SysConstant.INTEGER_defaultValue;
	/** 今日投资人数 */
	private Integer todayInvestorAmount = SysConstant.INTEGER_defaultValue;
	/** 今日新增投资 */
	private BigDecimal todayAddedInvestAmount = SysConstant.BIGDECIMAL_defaultValue;

	/** 昨日在线用户数(活跃投资人数) */
	private Integer yesterdayOnline = SysConstant.INTEGER_defaultValue;
	/** 昨日投资金额 */
	private BigDecimal yesterdayInvestAmount = SysConstant.BIGDECIMAL_defaultValue;
	/** 昨日注册用户数 */
	private Integer yesterdayRegisterAmount = SysConstant.INTEGER_defaultValue;
	/** 昨日实名认证用户数 */
	private Integer yesterdayVerifiedInvestorAmount = SysConstant.INTEGER_defaultValue;
	/** 昨日投资人数 */
	private Integer yesterdayInvestorAmount = SysConstant.INTEGER_defaultValue;
	/** 昨日新增投资 */
	private BigDecimal yesterdayAddedInvestAmount = SysConstant.BIGDECIMAL_defaultValue;

	/** 昨日各渠道投资额排名前5 */
	private List<PlatformChartQueryRep> channelRank = new ArrayList<PlatformChartQueryRep>();

	/** 昨日产品新增投资排名前五 */
	private List<PlatformChartQueryRep> proInvestorRank = new ArrayList<PlatformChartQueryRep>();

	/** 平台交易额占比分析 */
	private List<PlatformChartQueryRep> tradeAmountAnalyse = new ArrayList<PlatformChartQueryRep>();

	/** 平台交易额各渠道占比 */
	private List<PlatformChartQueryRep> channelAnalyse = new ArrayList<PlatformChartQueryRep>();

	/** 投资人质量分析 */
	private List<PlatformChartQueryRep> investorAnalyse = new ArrayList<PlatformChartQueryRep>();

}
