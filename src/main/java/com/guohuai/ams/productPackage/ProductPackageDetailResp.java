package com.guohuai.ams.productPackage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.ams.product.ProductEnum;
import com.guohuai.ams.product.reward.ProductRewardResp;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.file.FileResp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ProductPackageDetailResp extends BaseResp {

	public ProductPackageDetailResp(ProductPackage p,String productEleOid,String productIntroOid,String productActivityOid,String productElementTitle,String productIntroTitle,String activityDetailTitle) {
		this.oid = p.getOid();
		this.code = p.getCode();
		this.name = p.getName();
		this.fullName = p.getFullName();
		this.administrator = p.getAdministrator();// 管理人
		if (null != p.getType()) {
			this.typeOid = p.getType().getOid();// 产品类型
			this.typeName = p.getType().getName();
		}
//		this.type = p.getType();

		this.reveal = p.getReveal();// 额外增信
		this.revealComment = p.getRevealComment();// 增信备注
		this.currency = p.getCurrency();// 币种
		this.incomeCalcBasis = p.getIncomeCalcBasis();// 收益计算基础
		this.operationRate = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getOperationRate()));// 平台运营费率

		if (p.getAssetPool() != null) {
			this.assetPoolOid = p.getAssetPool().getOid();
			this.assetPoolName = p.getAssetPool().getName();
			if (p.getAssetPool().getSpvEntity() != null) {
				this.spvOid = p.getAssetPool().getSpvEntity().getOid();
			}
		}
		this.raiseStartDate = p.getRaiseStartDate() != null ? DateUtil.formatDate(p.getRaiseStartDate().getTime()) : "";// 募集开始时间
		this.raiseStartDateType = p.getRaiseStartDateType();
		this.raisePeriod = p.getRaisePeriodDays();// 募集期:()个自然日
		this.interestsFirstDate = p.getInterestsFirstDays();// 起息日:募集满额后()个自然日
		this.interestsDate = p.getInterestsFirstDays();// 起息日:募集满额后()个自然日
		this.foundDays = p.getFoundDays();// 募集期满后最晚成立日
		this.durationPeriod = p.getDurationPeriodDays();// 存续期:()个自然日
		this.expAror = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getExpAror()));// 预期年化收益率
		this.expArorSec = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getExpArorSec()));// 预期年化收益率区间
		if (p.getExpAror() != null) {
			if (p.getExpArorSec() != null && p.getExpAror().doubleValue() != p.getExpArorSec().doubleValue()) {
				this.expectedRate = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getExpAror())) + "~" + ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getExpArorSec()));
			} else {
				this.expectedRate = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getExpAror()));
			}
		}
		this.raisedTotalNumber = p.getRaisedTotalNumber();// 募集总份额
		this.accrualDate = p.getAccrualRepayDays();// 还本付息日 存续期结束后第()个自然日
		this.setupDate = p.getSetupDate() != null ? DateUtil.formatDate(p.getSetupDate().getTime()) : "";// 产品成立时间（存续期开始时间）
		this.setupDateType = p.getSetupDateType();

		this.accrualCycleOid = p.getAccrualCycleOid();

		this.accrualCycleName = (StringUtil.isEmpty(p.getAccrualCycleOid()) == true ? "" : ProductEnum.enums.get(p.getAccrualCycleOid()));//
		this.lockPeriod = p.getLockPeriodDays();
		this.purchaseConfirmDate = p.getPurchaseConfirmDays();// 申购确认日:()个
		this.purchaseConfirmDateType = p.getPurchaseConfirmDaysType();// 申购确认日类型:自然日或交易日
		this.redeemConfirmDate = p.getRedeemConfirmDays();// 赎回确认日:()个
		this.redeemConfirmDateType = p.getRedeemConfirmDaysType();// 赎回确认日类型:自然日或交易日
		this.netMaxRredeemDay = p.getNetMaxRredeemDay();// 单日净赎回上限
		this.minRredeem = p.getMinRredeem();
		this.maxRredeem = p.getMaxRredeem();
		this.additionalRredeem = p.getAdditionalRredeem();
		this.maxHold = p.getMaxHold();
		this.investMin = p.getInvestMin();// 单笔投资最低份额
		this.investMax = p.getInvestMax();// 单笔投资最高份额
		this.investAdditional = p.getInvestAdditional();// 单笔投资追加份额
		this.singleDailyMaxRedeem = p.getSingleDailyMaxRedeem();
		this.netUnitShare = ProductDecimalFormat.format(p.getNetUnitShare(), "0.####");// 单位份额净值
		this.maxSaleVolume = p.getMaxSaleVolume();
		this.investComment = p.getInvestComment();// 投资标的
		this.instruction = p.getInstruction();// 产品说明
		this.riskLevel = p.getRiskLevel();// 风险等级
		this.investorLevel = p.getInvestorLevel();// 投资者类型
		this.fileKeys = p.getFileKeys();// 附加文件
		this.status = p.getState();// 产品状态
		this.createTime = DateUtil.formatDatetime(p.getCreateTime().getTime());// 创建时间
		this.updateTime = DateUtil.formatDatetime(p.getUpdateTime().getTime());// 更新时间
		this.operator = p.getOperator();// 操作员
		this.auditState = p.getAuditState();// 审核状态
		this.isOpenPurchase = p.getIsOpenPurchase();// 开放申购期
		this.isOpenRemeed = p.getIsOpenRemeed();// 开放赎回期
		this.isOpenRedeemConfirm = p.getIsOpenRedeemConfirm();// 是否屏蔽赎回确认
		this.currentVolume = p.getCurrentVolume();// 当前份额
		this.collectedVolume = p.getCollectedVolume();// 已募份额
		this.purchaseNum = p.getPurchaseNum();// 已投次数
		this.lockCollectedVolume = p.getLockCollectedVolume();// 锁定已募份额
		this.raiseEndDate = p.getRaiseEndDate() != null ? DateUtil.formatDate(p.getRaiseEndDate().getTime()) : "";// 募集结束时间
		this.raiseFailDate = p.getRaiseFailDate() != null ? DateUtil.formatDate(p.getRaiseFailDate().getTime()) : "";// 募集宣告失败时间
		this.durationPeriodEndDate = p.getDurationPeriodEndDate() != null ? DateUtil.formatDate(p.getDurationPeriodEndDate().getTime()) : "";// 存续期结束时间
		this.accrualLastDate = p.getRepayDate() != null ? DateUtil.formatDate(p.getRepayDate().getTime()) : "";// 到期还款时间
		this.endDate = p.getEndDate() != null ? DateUtil.formatDate(p.getEndDate().getTime()) : "";// 产品清算（结束）时间
		this.durationRepaymentTime = p.getDurationRepaymentDate() != null ? DateUtil.formatDate(p.getDurationRepaymentDate().getTime()) : "";// 存续期内还款时间
		this.stems = p.getStems();// 来源
		
		this.isAutoAssignIncome = p.getIsAutoAssignIncome()==null?ProductPackage.NO:p.getIsAutoAssignIncome();
		this.isAutoAssignIncomeStr = ProductPackage.YES.equals(p.getIsAutoAssignIncome())?"是":"否";
		
		this.riskLevelStr = (StringUtil.isEmpty(p.getRiskLevel()) == true ? "" : ProductEnum.enums.get(p.getRiskLevel()));// 风险等级
		this.investorLevelStr = (StringUtil.isEmpty(p.getInvestorLevel()) == true ? "" : ProductEnum.enums.get(p.getInvestorLevel()));// 投资者类型
		this.revealStr = (StringUtil.isEmpty(p.getReveal()) == true ? "" : ProductEnum.enums.get(p.getReveal()));// 额外增信
		this.currencyStr = (StringUtil.isEmpty(p.getCurrency()) == true ? "" : ProductEnum.enums.get(p.getCurrency()));
		this.incomeCalcBasisStr = (StringUtil.isEmpty(p.getIncomeCalcBasis()) == true ? "" : p.getIncomeCalcBasis() + "(天)");// 收益计算基础
		this.operationRateStr = StringUtil.isEmpty(this.operationRate) ? "" : this.operationRate + "%";// 平台运营费率

		if ("FIRSTRACKTIME".equals(p.getRaiseStartDateType()) && "".equals(this.raiseStartDate)) {
			this.raiseStartDateStr = "与首次上架时间同时";
		} else {
			this.raiseStartDateStr = this.raiseStartDate;
		}

		this.raisePeriodStr = p.getRaisePeriodDays() != null ? p.getRaisePeriodDays() + "日" : "";// 募集期:()个自然日

		this.interestsFirstDateStr = p.getInterestsFirstDays() != null ? "成立后的第" + p.getInterestsFirstDays() + "日" : "";
		if(p.getFoundDays()!=null) {
			this.foundDaysStr = "募集期满后"+p.getFoundDays()+"日内";
		}
		this.durationPeriodStr = p.getDurationPeriodDays() != null ? p.getDurationPeriodDays() + "日" : "";// 存续期:()个自然日
		this.raisedTotalNumberStr = p.getRaisedTotalNumber() != null ? ProductDecimalFormat.format(p.getRaisedTotalNumber(), "0.####") + "份" : "";// 募集总份额
		
		this.currentVolumeStr = p.getCurrentVolume() != null ? ProductDecimalFormat.format(p.getCurrentVolume(), "0.####") + "份" : "";// 当前份额
		this.collectedVolumeStr = p.getCollectedVolume() != null ? ProductDecimalFormat.format(p.getCollectedVolume(), "0.####") + "份" : "";// 已募份额
		this.lockCollectedVolumeStr = p.getLockCollectedVolume() != null ? ProductDecimalFormat.format(p.getLockCollectedVolume(), "0.####") + "份" : "";// 锁定已募份额
		this.maxSaleVolumeStr = p.getMaxSaleVolume() != null ? ProductDecimalFormat.format(p.getMaxSaleVolume(), "0.####") + "份" : "";// 最高可售份额(申请的)

		this.accrualDateStr = p.getAccrualRepayDays() != null ? "存续期结束的后" + p.getAccrualRepayDays() + "日内" : "";
		this.lockPeriodStr = p.getLockPeriodDays() != null ? "一旦申购，将冻结此金额" + p.getLockPeriodDays() + "日" : "";
		this.purchaseConfirmDateStr = p.getPurchaseConfirmDays() != null ? ("申购订单提交后" + p.getPurchaseConfirmDays() + "日内") : "";

		this.redeemConfirmDateStr = p.getRedeemConfirmDays() != null ? ("赎回订单提交后" + p.getRedeemConfirmDays() + "日内") : "";

		this.netMaxRredeemDayStr = p.getNetMaxRredeemDay() != null && p.getNetMaxRredeemDay().compareTo(BigDecimal.ZERO) != 0 ? ProductDecimalFormat.format(p.getNetMaxRredeemDay(), "0.####") + "份" : "--";
		this.maxHoldStr = p.getMaxHold() != null && p.getMaxHold().compareTo(BigDecimal.ZERO) != 0 ? ProductDecimalFormat.format(p.getMaxHold(), "0.####") + "份" : "--";
		this.singleDailyMaxRedeemStr = p.getSingleDailyMaxRedeem() != null && p.getSingleDailyMaxRedeem().compareTo(BigDecimal.ZERO) != 0 ? ProductDecimalFormat.format(p.getSingleDailyMaxRedeem(), "0.####") + "份" : "--";

		this.minRredeemStr = p.getMinRredeem() != null && p.getMinRredeem().compareTo(BigDecimal.ZERO) != 0 ? ProductDecimalFormat.format(p.getMinRredeem(), "0.####") + "份" : "--";
		this.maxRredeemStr = p.getMaxRredeem() != null && p.getMaxRredeem().compareTo(BigDecimal.ZERO) != 0 ? ProductDecimalFormat.format(p.getMaxRredeem(), "0.####") + "份" : "--";
		this.additionalRredeemStr = p.getAdditionalRredeem() != null && p.getAdditionalRredeem().compareTo(BigDecimal.ZERO) != 0 ? ProductDecimalFormat.format(p.getAdditionalRredeem(), "0.####") + "份" : "--";
		this.investMinStr = p.getInvestMin() != null && p.getInvestMin().compareTo(BigDecimal.ZERO) != 0 ? ProductDecimalFormat.format(p.getInvestMin(), "0.####") + "份" : "--";
		this.investMaxStr = p.getInvestMax() != null && p.getInvestMax().compareTo(BigDecimal.ZERO) != 0 ? ProductDecimalFormat.format(p.getInvestMax(), "0.####") + "份" : "--";
		this.investAdditionalStr = p.getInvestAdditional() != null && p.getInvestAdditional().compareTo(BigDecimal.ZERO) != 0 ? ProductDecimalFormat.format(p.getInvestAdditional(), "0.####") + "份" : "--";
		this.netUnitShareStr = !"".equals(this.netUnitShare) ? this.netUnitShare + "元" : "";
		if ("FIRSTRACKTIME".equals(p.getSetupDateType()) && "".equals(this.setupDate)) {
			this.setupDateStr = "与首次上架时间同时";
		} else {
			this.setupDateStr = this.setupDate;
		}

		if (null != p.getAssetPool()) {
			AssetPoolEntity ap = p.getAssetPool();
			BigDecimal hqla = BigDecimal.ZERO;
			if (null != ap.getCashPosition()) {
				hqla = hqla.add(ap.getCashPosition());
			}
			if (null != ap.getScale() && null != ap.getCashtoolRate()) {
				hqla = hqla.add(ap.getScale().multiply(ap.getCashtoolRate()).setScale(2, RoundingMode.HALF_UP));
			}
			hqla = hqla.setScale(2, RoundingMode.HALF_UP);
			this.hqla = hqla;
			this.hqlaStr = hqla.toString();
		}

		if ("T".equals(p.getInvestDateType())) {
			this.investDateType = p.getInvestDateType();
			this.investDateTypeStr = "交易日";
		}
		if ("D".equals(p.getInvestDateType())) {
			this.investDateType = p.getInvestDateType();
			this.investDateTypeStr = "自然日";
		}

		if ("T".equals(p.getRredeemDateType())) {
			this.rredeemDateType = p.getRredeemDateType();
			this.rredeemDateTypeStr = "交易日";
		}
		if ("D".equals(p.getRredeemDateType())) {
			this.rredeemDateType = p.getRredeemDateType();
			this.rredeemDateTypeStr = "自然日";
		}
		
		this.dealStartTime = p.getDealStartTime();
		this.dealEndTime = p.getDealEndTime();
		this.fastRedeemStatus = p.getFastRedeemStatus();
		this.fastRedeemMax = p.getFastRedeemMax();
		
		if (p.getRecPeriodExpAnYield() != null) {
			this.recPeriodExpAnYield = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getRecPeriodExpAnYield()));// 募集期预期年化收益
			this.recPeriodExpAnYieldStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getRecPeriodExpAnYield())) + "%";// 募集期预期年化收益
			
		}
		this.subscribeConfirmDays = p.getPurchaseConfirmDays();// 认购确认日:认购订单提交后()个日内 
		this.subscribeConfirmDaysStr = p.getPurchaseConfirmDays() != null ? ("认购订单提交日后的" + p.getPurchaseConfirmDays() + "日内") : "";

		this.autoFoundDays = p.getAutoFoundDays();
		this.raiseFullFoundType = p.getRaiseFullFoundType();//募集满额后是否自动触发成立
		if ("AUTO".equals(p.getRaiseFullFoundType())) {//募集满额后是否自动触发成立
			if(p.getAutoFoundDays() != null && p.getAutoFoundDays().intValue()>0) {
				this.raiseFullFoundTypeStr = "募集满额后的第"+p.getAutoFoundDays()+"个自然日后自动成立";
			} else {
				this.raiseFullFoundTypeStr = "募集满额后的第1个自然日自动成立";
			}
		} else {
			this.raiseFullFoundTypeStr = "否";
		}
		this.singleDayRedeemCount = p.getSingleDayRedeemCount();
		
		/** 产品包新加20170524 **/
		this.productCount = p.getProductCount();
		this.singleProductVolume = p.getSingleProductVolume();
		this.limitTime = p.getLimitTime();// 募集期到期前()小时内不能上架
		this.limitTimeStr = p.getLimitTime() != null ? ("募集期到期前" + p.getLimitTime() + "小时内不能上架") : "";
		this.productElementStr = productElementTitle;//产品要素的ID
		this.productIntroStr = productIntroTitle;//产品说明ID
		this.activityDetailStr = activityDetailTitle;//活动产品详情介绍ID
		this.isActivityProduct = p.getIsActivityProduct();
		this.isActivityProductStr = p.getIsActivityProduct()==1?"是":"否";//是否为活动产品
		this.expectedArrorDispStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getExpectedArrorDisp()));//折合年化收益率
		/** 产品包新加20170524 **/
		this.productElementOid = productEleOid;
		this.productIntroOid = productIntroOid;
		this.activityDetailOid = productActivityOid;
		/**
		 * P2P相关
		 */
		// 是否是p2p产品
		this.ifP2P = p.getIfP2P();
		// 是否是p2p产品
		this.ifP2PStr = p.getIfP2P()==1?"是":"否";
		// 产品分类
		this.isP2PAssetPackage = p.getIsP2PAssetPackage();
		// 产品分类
		switch (p.getIsP2PAssetPackage()) {
			case 1:
				isP2PAssetPackageStr = "P2P资产包债匹";
				break;
			case 2:
				isP2PAssetPackageStr = "P2P企业散标";
				break;
			default:
				isP2PAssetPackageStr = "普通产品";
		}
	}

	private String oid;
	private String code;
	private String name;// 产品名称
	private String fullName;// 产品全称
	private String administrator;// 管理人
	private String typeOid;// 产品类型
	private String typeName;// 产品类型
//	private String type;
	private String reveal;// 额外增信
	private String revealComment;// 增信备注
	private String currency;// 币种
	private String incomeCalcBasis;// 收益计算基础
	private String operationRate;// 平台运营费率
	private String assetPoolOid;// 资产池Oid
	private String assetPoolName;// 资产池名称
	private String spvOid;// SPV Oid
	private String spvName;// SPV名称
	private String raiseStartDate;// 募集开始时间
	private String raiseStartDateType;// 募集开始时间类型
	private Integer raisePeriod;// 募集期:()个自然日
	private Integer interestsFirstDate;// 起息日:募集满额后()个自然日
	private Integer durationPeriod;// 存续期:()个自然日
	private String expectedRate;// 预期年化收益率
	private String expAror;// 预期年化收益率
	private String expArorSec;// 预期年化收益率区间
	private BigDecimal raisedTotalNumber;// 募集总份额
	private Integer accrualDate;// 还本付息日 存续期结束后第()个自然日
	private String setupDate;// 产品成立时间（存续期开始时间）
	private String setupDateType;// 产品成立时间类型
	private String accrualCycleOid;// 收益结转周期
	private String accrualCycleName;// 收益结转周期
	private Integer lockPeriod;// 锁定期:()个自然日 一旦申购，将冻结此金额T+5天。
	private Integer purchaseConfirmDate;// 申购确认日:()个
	private String purchaseConfirmDateType;// 申购确认日类型:自然日或交易日
	private Integer redeemConfirmDate;// 赎回确认日:()个
	private String redeemConfirmDateType;// 赎回确认日类型:自然日或交易日
	private BigDecimal netMaxRredeemDay;// 单日净赎回上限
	private BigDecimal minRredeem;// 单笔净赎回下限
	private BigDecimal maxRredeem;
	private BigDecimal additionalRredeem;
	private BigDecimal investMin;// 单笔投资最低份额
	private BigDecimal investMax;// 单笔投资最高份额
	private BigDecimal investAdditional;// 单笔投资追加份额
	private BigDecimal maxSaleVolume;// 最高可售份额(申请的)
	private BigDecimal maxHold;
	private BigDecimal singleDailyMaxRedeem;// 单人单日赎回上限

	private String netUnitShare;// 单位份额净值
	private String investComment;// 投资标的
	private String instruction;// 产品说明
	private String riskLevel;// 风险等级
	private String investorLevel;// 投资者类型
	private String fileKeys;// 附加文件
	private String status;// 产品状态
	private String createTime;// 创建时间
	private String updateTime;// 更新时间
	private String operator;// 操作员
	private String auditState;// 审核状态
	private String isOpenPurchase;// 开放申购期
	private String isOpenRemeed;// 开放赎回期
	private List<FileResp> files;// 附件
	private List<FileResp> investFiles;// 投资协议书
	private List<FileResp> serviceFiles;// 信息服务协议
	
	private Integer redeemTimingTaskDate;// 赎回定时任务天数 默认每日
	private BigDecimal currentVolume;// 当前份额
	private BigDecimal collectedVolume;// 已募份额
	private Integer purchaseNum;// 已投次数
	private BigDecimal lockCollectedVolume;// 锁定已募份额

	private String raiseEndDate;// 募集结束时间
	private String raiseFailDate;// 募集宣告失败时间
	private String durationPeriodEndDate;// 存续期结束时间
	private String accrualLastDate;// 到期还款时间
	private String endDate;// 产品清算（结束）时间
	private String durationRepaymentTime;// 存续期内还款时间
	private String stems;// 来源
	private String isDeleted;
	private String basicProductLabelOid;//基础标签
	private String basicProductLabelName;//基础标签
	private String[] expandProductLabelOids;//扩展标签
	private String[] expandProductLabelNames;//扩展标签
	private String currentVolumeStr;// 当前份额
	private String collectedVolumeStr;// 已募份额
	private String lockCollectedVolumeStr;// 锁定已募份额
	private String maxSaleVolumeStr;// 最高可售份额(申请的)
	private String riskLevelStr;// 风险等级
	private String investorLevelStr;// 投资者类型
	private String revealStr;// 额外增信
	private String currencyStr;// 币种
	private String incomeCalcBasisStr;// 收益计算基础
	private String operationRateStr;// 平台运营费率
	private String raiseStartDateStr;// 募集开始时间类型
	private String raisePeriodStr;// 募集期:()个自然日
	private String interestsFirstDateStr;// 起息日:募集满额后()个自然日
	private Integer interestsDate;// 起息日:募集满额后()个自然日
	private Integer foundDays;// 募集期满后最晚成立日
	private String foundDaysStr;// 募集期满后最晚成立日
	private String interestsDateStr;// 起息日:募集满额后()个自然日
	private String durationPeriodStr;// 存续期:()个自然日
	private String raisedTotalNumberStr;
	private String accrualDateStr;
	private String setupDateStr;// 产品成立时间（存续期开始时间）
	private String payModeNameStr;// 付利方式
	private String lockPeriodStr;// 锁定期:()个自然日 一旦申购，将冻结此金额T+5天。
	private String purchaseConfirmDateStr;// 申购确认日类型:自然日或交易日
	private String redeemConfirmDateStr;// 赎回确认日类型:自然日或交易日
	private String netMaxRredeemDayStr;// 单日净赎回上限
	private String maxHoldStr;
	private String singleDailyMaxRedeemStr;// 单人单日赎回上限

	private String minRredeemStr;// 单笔净赎回下限
	private String maxRredeemStr;
	private String additionalRredeemStr;
	private String investMinStr;// 单笔投资最低份额
	private String investMaxStr;// 单笔投资最高份额
	private String investAdditionalStr;// 单笔投资追加份额
	private String netUnitShareStr;// 单位份额净值

	private List<ProductRewardResp> rewards;// 奖励收益

	private BigDecimal hqla; // 流动性资产
	private String hqlaStr; // 流动性资产

	private String investDateType; // 有效投资日
	private String investDateTypeStr;
	private String rredeemDateType; // 有效赎回日
	private String rredeemDateTypeStr;

	private BigDecimal singleDailyLimitMaxRedeem;// 单人单日限额赎回上限
	private String isOpenLimitRedeem;// 是否开启限额赎回
	private String isOpenRedeemConfirm;// 是否屏蔽赎回确认
	
	private String dealStartTime;
	private String dealEndTime;
	
	private String fastRedeemStatus;//快速赎回开关 打开：YES 关闭：NO
	private BigDecimal fastRedeemMax;//快速赎回阀值	
	
	private String recPeriodExpAnYield;//募集期预期年化收益
	private Integer subscribeConfirmDays;// 认购确认日:认购订单提交后()个日内 
	private String raiseFullFoundType;//募集满额后是否自动触发成立
	private Integer autoFoundDays;//募集满额后第()个自然日后自动成立
	
	private String recPeriodExpAnYieldStr;//募集期预期年化收益
	private String subscribeConfirmDaysStr;// 认购确认日:认购订单提交后()个日内 
	private String raiseFullFoundTypeStr;//募集满额后是否自动触发成立
	
	private String isAutoAssignIncome;
	private String isAutoAssignIncomeStr;
	private Integer singleDayRedeemCount; // 单人单日赎回次数
	
	/** 产品包新加20170524  **/
	private Integer productCount;//产品包包含的产品数量
	private BigDecimal singleProductVolume;//产品包包含的单个产品份额
	private Integer toProductNum;//本资产包执行到的产品数量
	private String limitTime;//募集期到期前几小时内不能发布新产品
	private String limitTimeStr;//募集期到期前几小时内不能发布新产品
	private String[] channelOids;//扩展标签
	private String[] channelNames;//扩展标签
	/** 产品包新加20170524  **/
	/** 竞猜宝增加*/
	private String relateGuess;//是否关联竞猜宝（是/否）
	private String guessName;//竞猜宝名称
	private String guessOid;//竞猜宝Oid
	/** 竞猜宝增加*/
	/** 卡券添加 **/
	//红包相关
	private Integer[] redPackageOids;
	private String[] redPackageNames;
	private String usered;//是否可以使用红包描述
	private Integer useredId;//是否可以使用红包枚举项
	
	//加息券相关
	private Integer[] raiseRateCouponOids;
	private String[] raiseRateCouponNames;
	private String useRaise;//是否可以使用加息券描述
	private Integer useRaiseId;//是否可以使用加息券枚举项
	
	/**
	 * 关联产品详情
	 */
	private String productElementStr;//产品要素的标题
	private String productIntroStr;//产品说明标题
	private String activityDetailStr;//活动产品详情介绍标题
	private String productElementOid;//产品要素的ID
	private String productIntroOid;//产品说明ID
	private String activityDetailOid;//活动产品详情介绍ID

	private Integer isActivityProduct;//是否为活动产品
	private String isActivityProductStr;//是否为活动产品
	private String expectedArrorDispStr;//折合年化收益率
	/** 卡券添加**/

	/**
	 * P2P相关
	 */
	//是否是p2p产品
	private Integer ifP2P;
	//是否是p2p产品
	private String ifP2PStr;
	//是否是p2p资产包（债匹）
	private Integer isP2PAssetPackage;
	//是否是p2p资产包（债匹）
	private String isP2PAssetPackageStr;

}
