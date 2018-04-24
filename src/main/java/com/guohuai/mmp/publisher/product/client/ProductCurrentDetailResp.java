package com.guohuai.mmp.publisher.product.client;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import com.guohuai.ams.label.LabelResp;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.file.FileResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProductCurrentDetailResp extends BaseRep {

	public ProductCurrentDetailResp(Product p) {
		this.oid = p.getOid();// 产品oid
		this.type = p.getType().getOid();// 产品类型
		this.state = p.getState();// 产品状态
		this.productCode = p.getCode();
		this.productName = p.getName();
		this.productFullName = p.getFullName();
		if("YES".equals(p.getReveal())) {
			this.revealComment = p.getRevealComment();// 增信备注
		}
		this.incomeCalcBasis = p.getIncomeCalcBasis();
		this.interestsFirstDays = p.getInterestsFirstDays();// 起息日
		this.investMin = p.getInvestMin();// 单笔投资最低金额
		this.investAdditional = p.getInvestAdditional();// 单笔投资追加份额
		this.investMax = p.getInvestMax();// 投资最高份额
		this.currentVolume = p.getCurrentVolume();// 当前份额(投资者持有份额)
		this.raisedTotalNumber = p.getRaisedTotalNumber();// 募集总金额
		this.maxSaleVolume = p.getMaxSaleVolume();// 最高可售份额(申请的)(剩余)
		this.collectedVolume = p.getCollectedVolume();// 已集总金额
		this.lockCollectedVolume = p.getLockCollectedVolume();// 锁定已募份额(申购冻结金额)
		this.netUnitShare = p.getNetUnitShare();// 单位份额净值
		this.investComment = p.getInvestComment();// 投资标的
		this.instruction = p.getInstruction();// 产品说明
		this.riskLevel = p.getRiskLevel();// 风险等级
		this.investorLevel = p.getInvestorLevel();// 投资者类型
		this.fileKeys = p.getFileKeys();// 附加文件
		this.investFileKey = p.getInvestFileKey();// 投资协议书
		this.serviceFileKey = p.getServiceFileKey();// 信息服务协议
		this.newestProfitConfirmDate = p.getNewestProfitConfirmDate();// 最新收益确认日期
		this.isOpenPurchase = p.getIsOpenPurchase();// 开放申购期
		this.purchasePeopleNum = p.getPurchasePeopleNum();// 申购人数
		this.dealStartTime = p.getDealStartTime();// 交易开始时间
		this.dealEndTime = p.getDealEndTime();// 交易结束时间
		this.accrualCycleOid = p.getAccrualCycleOid();
		this.lockPeriodDays = p.getLockPeriodDays();
		this.productType = p.getType().getOid();
		this.reveal = p .getReveal();//额外增信
		if(p.getManageRate()!=null && p.getManageRate().compareTo(new BigDecimal("0"))>0) {
			this.manageRate = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getManageRate()))+"%";//托管费率
		}
		if(p.getFixedManageRate()!=null && p.getFixedManageRate().compareTo(new BigDecimal("0"))>0) {
			this.fixedManageRate = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getFixedManageRate()))+"%";//固定管理费率
		}
		if(p.getBasicRatio()!=null && p.getBasicRatio().compareTo(new BigDecimal("0"))>0) {
			this.basicRatio = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getBasicRatio()))+"%";//基础收益率
		}
		if(p.getOperationRate()!=null && p.getOperationRate().compareTo(new BigDecimal("0"))>0) {
			this.operationRate = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getOperationRate()))+"%";//平台运营费率
		}
		this.investDateType = p.getInvestDateType();//有效投资日类型
		this.minRredeem = p.getMinRredeem();//单笔净赎回下限
		this.maxRredeem = p.getMaxRredeem();//单笔净赎回上限
		this.additionalRredeem = p.getAdditionalRredeem();//单笔赎回追加份额
		this.rredeemDateType = p.getRredeemDateType();
		this.netMaxRredeemDay = p.getNetMaxRredeemDay();//单日净赎回上限
		this.maxHold = p.getMaxHold();//单人持有上限
		this.dailyNetMaxRredeem = p.getDailyNetMaxRredeem();//剩余赎回金额
		this.accrualRepayDays = p.getAccrualRepayDays();//还本付息日
		this.purchaseConfirmDays = p.getPurchaseConfirmDays();//申购确认日
		this.redeemConfirmDays = p.getRedeemConfirmDays();//赎回确认日
		this.isDeleted = p.getIsDeleted();//是否删除
		this.purchaseNum = p.getPurchaseNum();//已投次数
		this.repayDate = p.getRepayDate();//最迟还本付息日期
		this.repayInterestStatus = p.getRepayInterestStatus();//付息状态
		this.isOpenRemeed = p.getIsOpenRemeed();//开放赎回期
		this.singleDailyMaxRedeem = p.getSingleDailyMaxRedeem();//单人单日赎回上限
		this.isOpenRedeemConfirm = p.getIsOpenRedeemConfirm();//是否屏蔽赎回确认
		this.fastRedeemStatus = p.getFastRedeemStatus();
		this.fastRedeemMax = p.getFastRedeemMax();
		this.fastRedeemLeft = p.getFastRedeemLeft();
	}

	private String oid;// 产品oid
	private String type;// 产品类型
	private String state;// 产品状态
	private String productCode;// 产品编码
	private String productName;// 产品名称
	private String productFullName;// 产品全称
	private String revealComment;// 增信备注
	private String incomeCalcBasis;//收益计算基础
	private Integer lockPeriodDays;//锁定期
	private Integer interestsFirstDays;// 起息日
	private Date interestsFirstDate;//起息日期
	private Date setupDate;// 成立日期
	private String accrualCycleOid;//[D] 收益结转周期
	private BigDecimal investMin;// 单笔投资最低金额
	private BigDecimal investAdditional;// 单笔投资追加份额
	private BigDecimal investMax;// 投资最高份额

	private BigDecimal currentVolume;// 当前份额(投资者持有份额)
	private BigDecimal raisedTotalNumber;// 募集总金额
	private BigDecimal maxSaleVolume;// 最高可售份额(申请的)(剩余)
	private BigDecimal collectedVolume;// 已集总金额
	private BigDecimal lockCollectedVolume;// 锁定已募份额(申购冻结金额)

	private BigDecimal netUnitShare;// 单位份额净值
	private String investComment;// 投资标的
	private String instruction;// 产品说明
	private String riskLevel;// 风险等级
	private String investorLevel;// 投资者类型
	private String fileKeys;// 附加文件
	private String investFileKey;// 投资协议书
	private String serviceFileKey;// 信息服务协议
	private Date newestProfitConfirmDate;// 最新收益确认日期
	private String isOpenPurchase;// 开放申购期

	private Integer purchasePeopleNum;// 申购人数
	private String dealStartTime;// 交易开始时间
	private String dealEndTime;// 交易结束时间
	
	private List<FileResp> files;// 附件
	private List<FileResp> investFiles;// 投资协议书
	private List<FileResp> serviceFiles;// 信息服务协议
	
	private List<ProductDetailIncomeRewardProfit> rewardYields;//昨日年化收益率走势  单位（%） :昨日年化收益率+奖励收益率
	private List<CurrentProductDetailProfit> perMillionIncomes;//基准每万份收益 单位（元）
	private List<CurrentProductDetailProfit> annualYields;//基准年化收益率走势  单位（%）
	
	private String annualInterestSec;// 预期年化收益率区间
	private String tenThsPerDayProfit;//万份收益
	private String sevenDayYield;//七日年化收益率
	private String yesterdayYield;//昨日年化收益率
	private String rewardYieldRange;//奖励收益率区间
	private String rewardTenThsProfit;//奖励万份收益
	private String showType;//展示类型
	private List<LabelResp> productLabels;//产品标签
	
	private String productType;
	private String reveal;//额外增信
	private String manageRate;//托管费率
	private String fixedManageRate;//固定管理费率
	private String basicRatio;//基础收益率
	private String operationRate;//平台运营费率
	private String investDateType;//有效投资日类型
	private BigDecimal minRredeem;//单笔净赎回下限
	private BigDecimal maxRredeem;//单笔净赎回上限
	private BigDecimal additionalRredeem;//单笔赎回追加份额
	private String rredeemDateType;
	private BigDecimal netMaxRredeemDay;//单日净赎回上限
	private BigDecimal maxHold;//单人持有上限
	private BigDecimal dailyNetMaxRredeem;//剩余赎回金额
	private Integer accrualRepayDays;//还本付息日
	private Integer purchaseConfirmDays;//申购确认日
	private Integer redeemConfirmDays;//赎回确认日
	private String isDeleted;//是否删除
	private Integer purchaseNum;//已投次数
	private Date repayDate;//最迟还本付息日期
	private String repayInterestStatus;//付息状态
	private String isOpenRemeed;//开放赎回期
	private BigDecimal singleDailyMaxRedeem;//单人单日赎回上限
	private String isOpenRedeemConfirm;//是否屏蔽赎回确认
	
	/**
	 * 快速赎回开关	fastRedeemStatus	varchar(32)	32		FALSE	FALSE	FALSE
	 *打开：YES 关闭：NO
		快速赎回阀值	fastRedeemMax	decimal(20,4)	20	4	FALSE	FALSE	FALSE
	 */
	private String fastRedeemStatus;
	private BigDecimal fastRedeemMax;
	private BigDecimal fastRedeemLeft;
	private String companyName;
	/**
	 * 产品属性：单人单日赎回次数
	 */
	private Integer singleDayRedeemCount;
	
	/**
	 * 兼容老接口
	 */
	private String isShowRedPackets;  // 是否显示红包（YES:显示  NO:不显示）
	
	/**
	 * 卡券相关
	 */
	private Integer isShowRedPacketsNew;  // 是否显示红包（1:可以使用，2:不能使用，3:全部可以使用）
	private Integer isShowRaiseCoupons;  // 是否显示加息券（1:可以使用，2:不能使用，3:全部可以使用）
	private List<Integer> redPackages;//红包列表
	private List<Integer> raiseCoupons;//加息券列表
}
