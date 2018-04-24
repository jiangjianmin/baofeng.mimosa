package com.guohuai.mmp.publisher.product.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import com.guohuai.ams.label.LabelResp;
import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.ams.product.productChannel.ProductChannel;
import com.guohuai.component.web.view.BaseRep;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProductPeriodicResp extends BaseRep {

	public ProductPeriodicResp(ProductChannel cp, Map<String,Map<String,BigDecimal>> productMinMaxRewardYeildMap) {
		String productOid = cp.getProduct().getOid();
		String incomeCalcBasis = cp.getProduct().getIncomeCalcBasis();//产品计算基础
		BigDecimal expAror = cp.getProduct().getExpAror();//预期年化收益率
		BigDecimal expArorSec = cp.getProduct().getExpArorSec();//预期年化收益率
		
		String expArorStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expAror))+"%";
		String expArorSecStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expArorSec))+"%";
		
		String tenThsPerDayProfitFst = ProductDecimalFormat.format(expAror.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
		String tenThsPerDayProfitSec = ProductDecimalFormat.format(expArorSec.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
		
		if(productMinMaxRewardYeildMap.get(productOid)!=null) {
			Map<String,BigDecimal> minMaxReward = productMinMaxRewardYeildMap.get(productOid);
			
			BigDecimal minReward = minMaxReward.get("minReward");
			BigDecimal maxReward = minMaxReward.get("maxReward");
			
			String minRewardStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(minReward))+"%";
			String maxRewardStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(maxReward))+"%";
			if(minRewardStr.equals(maxRewardStr)) {
				this.rewardYieldRange = minRewardStr;
			} else {
				this.rewardYieldRange = minRewardStr+"-"+maxRewardStr;
			}
			
			String rewardTenThsProfitFst = ProductDecimalFormat.format(minReward.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
			String rewardTenThsProfitSec = ProductDecimalFormat.format(maxReward.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
			if(rewardTenThsProfitFst.equals(rewardTenThsProfitSec)) {
				this.rewardTenThsProfit = rewardTenThsProfitFst;
			} else {
				this.rewardTenThsProfit = rewardTenThsProfitFst+"-"+rewardTenThsProfitSec;
			}
		}
		
		
		this.oid = productOid;
		this.channelOid = cp.getChannel().getOid();
		this.productCode = cp.getProduct().getCode();
		this.productName = cp.getProduct().getName();
		this.productFullName = cp.getProduct().getFullName();
		this.purchaseNum = cp.getProduct().getPurchaseNum();
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
		this.durationPeriod = cp.getProduct().getDurationPeriodDays();
		this.raisedTotalNumber = cp.getProduct().getRaisedTotalNumber();
		this.collectedVolume = cp.getProduct().getCollectedVolume();
		this.maxSaleVolume = cp.getProduct().getMaxSaleVolume();//最高可售份额
		this.lockCollectedVolume = cp.getProduct().getLockCollectedVolume();//锁定已募份额
		this.currentVolume = cp.getProduct().getCurrentVolume();//当前份额
		this.investMin = cp.getProduct().getInvestMin();
		this.state = cp.getProduct().getState();// 产品状态
		this.netUnitShare = cp.getProduct().getNetUnitShare();
	}
	
	private String oid;//产品oid
	private String state;//产品状态
	private String channelOid;//渠道oid
	private String productCode;//产品编码
	private String productName;//产品名称
	private String productFullName;//产品全称
	private Integer durationPeriod;//存续期:()个自然日
	private BigDecimal raisedTotalNumber;//募集总金额
	private BigDecimal maxSaleVolume;//最高可售份额
	private BigDecimal lockCollectedVolume;//锁定已募份额
	private BigDecimal currentVolume;//当前份额
	private BigDecimal collectedVolume;//已集总金额
	private BigDecimal investMin;//单笔投资最低金额
	private String annualInterestSec;//预期年化收益率区间
	private String tenThsPerDayProfit;//万份收益
	private String rewardYieldRange;//奖励收益率区间
	private String rewardTenThsProfit;//奖励万份收益
	private List<LabelResp> productLabels;//产品标签
	private BigDecimal netUnitShare;//单位份额净值
	private Integer purchaseNum;//已投次数

}
