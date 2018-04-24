package com.guohuai.mmp.publisher.product.client;

import java.math.BigDecimal;
import java.util.List;

import com.guohuai.ams.label.LabelResp;
import com.guohuai.component.web.view.BaseRep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProductCurrentResp extends BaseRep {
	
	public static final String SHOW_TYPE_1 = "1";//无奖励收益 有预期年化收益率并且是区间   展示 区间预期年化收益率 比如 预期年化收益率：1%-5%
	public static final String SHOW_TYPE_2 = "2";//无奖励收益 有预期年化收益率并且是固定值 展示 固定预期年化收益率 比如 预期年化收益率：1%
	public static final String SHOW_TYPE_3 = "3";//无奖励收益 无有预期年化收益率 展示 七日年化收益率 比如 七日年化收益率：2%
	public static final String SHOW_TYPE_4 = "4";//有奖励收益 有预期年化收益率并且是区间 展示 区间预期年化收益率和区间奖励收益率 比如 预期年化收益率+奖励收益率：1%-5% + 2%-10%
	public static final String SHOW_TYPE_5 = "5";//有奖励收益 有预期年化收益率并且是固定值 展示 固定预期年化收益率和区间奖励收益率 比如 预期年化收益率+奖励收益率：1% + 2%-10%
	public static final String SHOW_TYPE_6 = "6";//有奖励收益 无预期年化收益率但有基础收益（不固定）展示 昨日年化收益率和区间奖励收益率 比如 昨日年化收益率+奖励收益率：1% + 2%-10%
	public static final String SHOW_TYPE_7 = "7";//有奖励收益 无预期年化收益率无基础收益 展示 区间奖励收益率 比如 奖励收益率：2%-10%
	
	
	
	private String oid;//产品oid
	private String channelOid;//渠道oid
	private String productCode;//产品编码
	private String productName;//产品名称
	private String productFullName;//产品全称
	private String annualInterestSec;//预期年化收益率区间
	private String sevenDayYield;//七日年化收益率
	private String yesterdayYield;//昨日年化收益率
	private String rewardYieldRange;//奖励收益率区间
	private String tenThsPerDayProfit;//万份收益
	private String rewardTenThsProfit;//奖励万份收益
	private BigDecimal raisedTotalNumber;//募集总份额
	private BigDecimal maxSaleVolume;//最高可售份额
	private BigDecimal currentVolume;//当前金额
	private BigDecimal collectedVolume;//已集总金额
	private BigDecimal lockCollectedVolume;//锁定已募份额
	private BigDecimal investMin;//单笔投资最低金额
	private Integer lockPeriodDays;//锁定期
	private String showType;//展示类型
	private List<LabelResp> productLabels;//产品标签
	private BigDecimal netUnitShare;//单位份额净值
	private Integer purchaseNum;//已投次数
	private String subType;
}
