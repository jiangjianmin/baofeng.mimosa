package com.guohuai.mmp.publisher.product.client;

import com.guohuai.ams.companyScatterStandard.LoanContractResp;
import com.guohuai.ams.label.LabelResp;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.ams.product.ProductPojo;
import com.guohuai.ams.product.reward.ProductIncomeReward;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.file.FileResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProductPeriodicDetailResp extends BaseRep {

	public ProductPeriodicDetailResp(Product p, List<ProductIncomeReward> prewards, String activityDetailUrl) {
		this.oid = p.getOid();// 产品oid
		this.type = p.getType().getOid();// 产品类型
		this.state = p.getState();// 产品状态
		this.productCode = p.getCode();
		this.productName = p.getName();
		this.productFullName = p.getFullName();
		this.purchaseNum = p.getPurchaseNum();
		
		BigDecimal expAror = p.getExpAror();
		BigDecimal expArorSec = p.getExpArorSec();
		
		String expArorStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expAror))+"%";
		String expArorSecStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expArorSec))+"%";
		
		String tenThsPerDayProfitFst = ProductDecimalFormat.format(expAror.multiply(new BigDecimal(10000)).divide(new BigDecimal(p.getIncomeCalcBasis()), 4, RoundingMode.HALF_UP),"0.0000");
		String tenThsPerDayProfitSec = ProductDecimalFormat.format(expArorSec.multiply(new BigDecimal(10000)).divide(new BigDecimal(p.getIncomeCalcBasis()), 4, RoundingMode.HALF_UP),"0.0000");
		
		if(expArorStr.equals(expArorSecStr)) {
			this.annualInterestSec = expArorStr;
		} else {
			this.annualInterestSec = expArorStr+"-"+expArorSecStr;
		}
		if(tenThsPerDayProfitFst.equals(tenThsPerDayProfitSec)) {
			this.tenThsPerDayProfit = tenThsPerDayProfitFst;
		} else {
			this.tenThsPerDayProfit = tenThsPerDayProfitFst+"-"+tenThsPerDayProfitSec;
		}
		
		if (prewards != null && prewards.size() > 0) {//算上奖励收益
			BigDecimal minReward = prewards.get(0).getRatio();
			BigDecimal maxReward = prewards.get(0).getRatio();
			for(ProductIncomeReward preward : prewards) {
				if(preward.getRatio().compareTo(minReward)<0) {
					minReward = preward.getRatio();
				}
				if(preward.getRatio().compareTo(maxReward)>0) {
					maxReward = preward.getRatio();
				}
			}
			
			String minRewardStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(minReward))+"%";
			String maxRewardStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(maxReward))+"%";
			
			String rewardTenThsProfitFst = ProductDecimalFormat.format(minReward.multiply(new BigDecimal(10000)).divide(new BigDecimal(p.getIncomeCalcBasis()), 4, RoundingMode.HALF_UP),"0.0000");
			String rewardTenThsProfitSec = ProductDecimalFormat.format(maxReward.multiply(new BigDecimal(10000)).divide(new BigDecimal(p.getIncomeCalcBasis()), 4, RoundingMode.HALF_UP),"0.0000");
			
			if(minRewardStr.equals(maxRewardStr)) {
				this.rewardYieldRange = minRewardStr;
			} else {
				this.rewardYieldRange = minRewardStr+"-"+maxRewardStr;
			}
			if(rewardTenThsProfitFst.equals(rewardTenThsProfitSec)) {
				this.rewardTenThsProfit = rewardTenThsProfitFst;
			} else {
				this.rewardTenThsProfit = rewardTenThsProfitFst+"-"+rewardTenThsProfitSec;
			}
		}
		
		if("YES".equals(p.getReveal())) {
			this.revealComment = p.getRevealComment();// 增信备注
		}
		this.incomeCalcBasis = p.getIncomeCalcBasis();
		this.raisePeriodDays = p.getRaisePeriodDays();// 募集期
		this.raiseStartDate = p.getRaiseStartDate();// 募集开始日
		this.raiseEndDate = p.getRaiseEndDate();// 募集结束日期
		this.interestsFirstDays = p.getInterestsFirstDays();// 起息日
		this.durationPeriod = p.getDurationPeriodDays();// 存续期:()个自然日
		this.setupDate = p.getSetupDate();// setupDate存续期开始日期
		if (p.getSetupDate()!=null) {
			this.interestsStartDate = new Date(DateUtil.addDay(new java.util.Date(p.getSetupDate().getTime()), p.getInterestsFirstDays()).getTime());// (起息开始日期)
		}
		this.interestsEndDate = p.getDurationPeriodEndDate();// durationPeriodEndDate存续期结束日期(起息结束日期)
		this.accrualDateDays = p.getAccrualRepayDays();//还本付息日 存续期结束后第()个自然日
		this.repayDate = p.getRepayDate();//到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止
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
		this.isOpenPurchase = p.getIsOpenPurchase();// 开放申购期
		this.purchasePeopleNum = p.getPurchasePeopleNum();// 申购人数
		this.dealStartTime = p.getDealStartTime();// 交易开始时间
		this.dealEndTime = p.getDealEndTime();// 交易结束时间
		this.expectedArrorDisp = ProductDecimalFormat.format(ProductDecimalFormat.multiply(p.getExpectedArrorDisp()))+"%";//折合年化收益率
		this.activityDetailUrl = activityDetailUrl;//产品详情URL
		if(p.getIsActivityProduct() == 0) {
			this.SubType = ProductPojo.DEPOSIT_SUBTYPE;
		}else {
			this.SubType = ProductPojo.ZERO_BUY_SUBTYPE;
		}
		//增加快定宝 07类型
		if ( Product.TYPE_Producttype_03.equals(p.getType().getOid())){
			this.SubType = ProductPojo.BF_PLUS_SUBTYPE;
		}
		this.isP2PAssetPackage = p.getIsP2PAssetPackage() == 0 ? 0 : 1;

	}

	private String oid;// 产品oid
	private String type;// 产品类型
	private String state;// 产品状态
	private String productCode;// 产品编码
	private String productName;// 产品名称
	private String productFullName;// 产品全称
	private String annualInterestSec;// 预期年化收益率区间
	private String tenThsPerDayProfit;//万份收益
	private String rewardYieldRange;//奖励收益率区间
	private String rewardTenThsProfit;//奖励万份收益
	private String revealComment;// 增信备注
	private String incomeCalcBasis;//收益计算基础
	private Integer raisePeriodDays;// 募集期
	private Date raiseStartDate;// 募集开始日
	private Date raiseEndDate;// 募集结束日期
	private Integer interestsFirstDays;// 起息日
	private Integer durationPeriod;// 存续期:()个自然日
	private Date interestsStartDate;//(起息开始日期)
	private Date setupDate;// 存续期开始日期
	private Date interestsEndDate;// durationPeriodEndDate存续期结束日期(起息结束日期)
	private Integer accrualDateDays;//还本付息日 存续期结束后第()个自然日
	private Date repayDate;//到期最晚还本付息日 指存续期结束后的还本付息最迟发生在存续期后的第X个自然日的23:59:59为止

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
	private String isOpenPurchase;// 开放申购期

	private Integer purchasePeopleNum;// 申购人数
	private String dealStartTime;// 交易开始时间
	private String dealEndTime;// 交易结束时间
	
	private List<FileResp> files;// 附件
	private List<FileResp> investFiles;// 投资协议书
	private List<FileResp> serviceFiles;// 信息服务协议
	private List<LabelResp> productLabels;//产品标签
	private Integer purchaseNum;//已投次数
	private Timestamp investTime;
	/**
	 * 卡券相关
	 */
	private Integer isShowRedPacketsNew;  // 是否显示红包（1:可以使用，2:不能使用，3:全部可以使用）
	private Integer isShowRaiseCoupons;  // 是否显示加息券（1:可以使用，2:不能使用，3:全部可以使用）
	private List<Integer> redPackages;//红包列表
	private List<Integer> raiseCoupons;//加息券列表
	/**
	 * 兼容老接口
	 */
	private String isShowRedPackets;  // 是否显示红包（YES:显示  NO:不显示）
	private String expectedArrorDisp;
	private String activityDetailUrl;
	private String SubType;

	/**
	 * P2P
	 */
	// 是否为债匹
	private Integer isP2PAssetPackage;

	/**
	 * 散标相关
	 */
	private LoanContractResp loanContract; // 借款方信息
	private String repayWay; // 还款方式
	private String endArragementDesc; // 到期安排
	private String investTimeDesc; // 投资日中文描述
	private String setupDateDesc; // 起息日中文描述
	private String durationPeriodEndDateDesc; // 到期日中文描述
	private boolean chinese; // 是否显示中文描述
}
