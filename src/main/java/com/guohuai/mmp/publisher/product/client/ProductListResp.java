package com.guohuai.mmp.publisher.product.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.ams.label.LabelResp;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDecimalFormat;
import com.guohuai.ams.product.productChannel.ProductChannel;
import com.guohuai.component.web.view.BaseRep;

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
public class ProductListResp extends BaseRep {
			
	public ProductListResp(ProductChannel cp, Map<String,Map<String,BigDecimal>> productExpArorMap, Page<IncomeAllocate> pcas) {
		Product product = cp.getProduct();
		String productOid = product.getOid();
		String incomeCalcBasis = product.getIncomeCalcBasis();//产品计算基础
		
		BigDecimal expAror = product.getExpAror();//预期年化收益率
		BigDecimal expArorSec = product.getExpArorSec();//预期年化收益率
		
		String expArorStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expAror))+"%";
		String expArorSecStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expArorSec))+"%";
		
		String tenThsPerDayProfitFst = ProductDecimalFormat.format(expAror.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
		String tenThsPerDayProfitSec = ProductDecimalFormat.format(expArorSec.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
		
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
		this.purchaseNum = product.getPurchaseNum();
		
		if(Product.TYPE_Producttype_02.equals(product.getType().getOid())) {
			if((product.getExpAror()!=null && product.getExpAror().compareTo(new BigDecimal("0"))>0)
					|| (product.getExpArorSec()!=null && product.getExpArorSec().compareTo(new BigDecimal("0"))>0)) {
				if(expArorStr.equals(expArorSecStr)) {//固定预期收益率 
					if(productExpArorMap.get(product.getOid())!=null) {
						this.showType = ProductCurrentResp.SHOW_TYPE_5;
					} else {
						this.showType = ProductCurrentResp.SHOW_TYPE_2;
					}
				} else {
					if(productExpArorMap.get(product.getOid())!=null) {
						this.showType = ProductCurrentResp.SHOW_TYPE_4;
					} else {
						this.showType = ProductCurrentResp.SHOW_TYPE_1;
					}
				}
			} else {
				if(productExpArorMap.get(product.getOid())!=null) {
					if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
						BigDecimal yesterdayYieldExp = pcas.getContent().get(0).getRatio();//昨日年化收益率
						this.yesterdayYield = ProductDecimalFormat.format(ProductDecimalFormat.multiply(yesterdayYieldExp))+"%";//昨日年化收益率
						this.tenThsPerDayProfit = ProductDecimalFormat.format(yesterdayYieldExp.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
						this.showType = ProductCurrentResp.SHOW_TYPE_6;
					} else {
						this.showType = ProductCurrentResp.SHOW_TYPE_7;
					}
				} else {
					if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
						BigDecimal sevenDayYieldRatio = new BigDecimal("0");
						for(IncomeAllocate ia : pcas.getContent()) {
							sevenDayYieldRatio = sevenDayYieldRatio.add(ia.getRatio());
						}
						BigDecimal sevenDayYieldExp = sevenDayYieldRatio.multiply(new BigDecimal("100")).divide(new BigDecimal(""+new Long(pcas.getTotalElements()).intValue()), 4, RoundingMode.HALF_UP);//七日年化收益率单位（%）
						this.sevenDayYield = ProductDecimalFormat.format(sevenDayYieldExp,"0.00")+"%";//七日年化收益率:最新7条取平均值
						this.tenThsPerDayProfit = ProductDecimalFormat.format(sevenDayYieldExp.divide(new BigDecimal("100")).multiply(new BigDecimal("10000")).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
					}
					this.showType = ProductCurrentResp.SHOW_TYPE_3;
				}
			}
		}
		if(productExpArorMap.get(productOid)!=null) {
			Map<String,BigDecimal> minMaxReward = productExpArorMap.get(productOid);
			
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
		this.type = product.getType().getOid();
		this.channelOid = cp.getChannel().getOid();
		this.productCode = product.getCode();
		this.productName = product.getName();
		this.productFullName = product.getFullName();
		this.currentVolume = product.getCurrentVolume();
		this.collectedVolume = product.getCollectedVolume();
		this.lockCollectedVolume = product.getLockCollectedVolume();// 锁定已募份额(申购冻结金额)
		this.investMin = product.getInvestMin();
		this.lockPeriodDays = product.getLockPeriodDays();
		this.durationPeriod = product.getDurationPeriodDays();
		this.raisedTotalNumber = product.getRaisedTotalNumber();
		this.maxSaleVolume = product.getMaxSaleVolume();
		this.state = product.getState();
		this.netUnitShare = product.getNetUnitShare();
	}
	
	public ProductListResp(Object[] o, Map<String,Map<String,BigDecimal>> productExpArorMap, Page<IncomeAllocate> pcas) {
		String productOid = (String)o[0];
		String incomeCalcBasis = (String)o[1];//产品计算基础
		
		BigDecimal expAror = (BigDecimal)o[3];//预期年化收益率
		BigDecimal expArorSec = (BigDecimal)o[4];//预期年化收益率
		String expArorStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expAror))+"%";
		String expArorSecStr = ProductDecimalFormat.format(ProductDecimalFormat.multiply(expArorSec))+"%";
		String tenThsPerDayProfitFst = ProductDecimalFormat.format(expAror.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
		String tenThsPerDayProfitSec = ProductDecimalFormat.format(expArorSec.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
		
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
		
		if(Product.TYPE_Producttype_02.equals((String)o[2])) {
			if((expAror!=null && expAror.compareTo(new BigDecimal("0"))>0)
					|| (expArorSec!=null && expArorSec.compareTo(new BigDecimal("0"))>0)) {
				if(tenThsPerDayProfitFst.equals(tenThsPerDayProfitSec)) {
					this.tenThsPerDayProfit = tenThsPerDayProfitFst;
				} else {
					this.tenThsPerDayProfit = tenThsPerDayProfitFst+"-"+tenThsPerDayProfitSec;
				}
				if(expArorStr.equals(expArorSecStr)) {//固定预期收益率 
					if(productExpArorMap.get(productOid)!=null) {
						this.showType = ProductCurrentResp.SHOW_TYPE_5;
					} else {
						this.showType = ProductCurrentResp.SHOW_TYPE_2;
					}
				} else {
					if(productExpArorMap.get(productOid)!=null) {
						this.showType = ProductCurrentResp.SHOW_TYPE_4;
					} else {
						this.showType = ProductCurrentResp.SHOW_TYPE_1;
					}
				}
			} else {
				if(productExpArorMap.get(productOid)!=null) {
					if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
						BigDecimal yesterdayYieldExp = pcas.getContent().get(0).getRatio();//昨日年化收益率
						this.yesterdayYield = ProductDecimalFormat.format(ProductDecimalFormat.multiply(yesterdayYieldExp))+"%";//昨日年化收益率
						this.tenThsPerDayProfit = ProductDecimalFormat.format(yesterdayYieldExp.multiply(new BigDecimal(10000)).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
						this.showType = ProductCurrentResp.SHOW_TYPE_6;
					} else {
						this.showType = ProductCurrentResp.SHOW_TYPE_7;
					}
				} else {
					if (pcas != null && pcas.getContent() != null && pcas.getTotalElements() > 0) {
						BigDecimal sevenDayYieldRatio = new BigDecimal("0");
						for(IncomeAllocate ia : pcas.getContent()) {
							sevenDayYieldRatio = sevenDayYieldRatio.add(ia.getRatio());
						}
						BigDecimal sevenDayYieldExp = sevenDayYieldRatio.multiply(new BigDecimal("100")).divide(new BigDecimal(""+new Long(pcas.getTotalElements()).intValue()), 4, RoundingMode.HALF_UP);//七日年化收益率单位（%）
						this.sevenDayYield = ProductDecimalFormat.format(sevenDayYieldExp,"0.00")+"%";//七日年化收益率:最新7条取平均值
						this.tenThsPerDayProfit = ProductDecimalFormat.format(sevenDayYieldExp.divide(new BigDecimal("100")).multiply(new BigDecimal("10000")).divide(new BigDecimal(incomeCalcBasis), 4, RoundingMode.HALF_UP),"0.0000");
					}
					this.showType = ProductCurrentResp.SHOW_TYPE_3;
				}
			}
		}
		if(productExpArorMap.get(productOid)!=null) {
			Map<String,BigDecimal> minMaxReward = productExpArorMap.get(productOid);
			
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
		this.type = (String)o[2];
		this.channelOid = (String)o[5];
		this.productCode = (String)o[6];
		this.productName = (String)o[7];
		this.productFullName = (String)o[8];
		this.currentVolume = (BigDecimal)o[9];
		this.collectedVolume = (BigDecimal)o[10];
		this.lockCollectedVolume = (BigDecimal)o[11];// 锁定已募份额(申购冻结金额)
		this.investMin = (BigDecimal)o[12];
		this.lockPeriodDays = (Integer)o[13];
		this.durationPeriod = (Integer)o[14];
		this.raisedTotalNumber = (BigDecimal)o[15];
		this.maxSaleVolume = (BigDecimal)o[16];
		this.state = (String)o[17];
		this.netUnitShare = (BigDecimal)o[18];
		this.purchaseNum = (Integer)o[19];
	}
	
	private String oid;//产品oid
	private String type;// 产品类型
	private String channelOid;//渠道oid
	private String productCode;//产品编码
	private String productName;//产品名称
	private String productFullName;//产品全称
	private String annualInterestSec;//预期年化收益率区间
	private String tenThsPerDayProfit;//万份收益
	private String rewardYieldRange;//奖励收益率区间
	private String rewardTenThsProfit;//奖励万份收益
	private BigDecimal netUnitShare;//单位份额净值
	private BigDecimal currentVolume;//当前金额
	private BigDecimal collectedVolume;//已集总金额
	private BigDecimal lockCollectedVolume;// 锁定已募份额(申购冻结金额)
	private BigDecimal investMin;//单笔投资最低金额
	private Integer lockPeriodDays;//锁定期
	private Integer durationPeriod;//存续期:()个自然日
	private BigDecimal raisedTotalNumber;//募集总金额
	private BigDecimal maxSaleVolume;// 最高可售份额(申请的)(剩余)
	private String showType;//展示类型
	private String yesterdayYield;//昨日年化收益率
	private String sevenDayYield;//七日年化收益率
	private List<LabelResp> productLabels;//产品标签
	private String state;
	private Integer purchaseNum;//已投次数

}

