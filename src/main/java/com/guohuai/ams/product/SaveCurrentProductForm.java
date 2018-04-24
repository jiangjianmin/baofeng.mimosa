package com.guohuai.ams.product;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import com.guohuai.component.web.parameter.validation.Enumerations;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveCurrentProductForm implements Serializable {

	private static final long serialVersionUID = -7236645696766816814L;

	private String oid;
	
	@NotBlank
	private String code; //产品编号
	@NotBlank
	private String name;// 产品简称
	@NotBlank
	private String fullName;// 产品全称
	
	@NotBlank
	private String typeOid;// 产品类型
	@NotBlank
	private String administrator;// 产品管理人
	
	@Length(min = 32, max = 32, message = "所属资产池长度为32个字符")
	private String assetPoolOid;// 所属资产池
	
	@NotNull(message = "预期年化收益率起始区间不能为空")
	private BigDecimal expAror;// 预期年化收益率
	private BigDecimal expArorSec;// 预期年化收益率区间
	private String incomeCalcBasis;// 收益计算基础
	
	
	private BigDecimal operationRate;// 平台运营费率
	@Enumerations(values = { "CNY", "USD", "EUR", "JPY", "GBP", "HKD", "SGD", "JMD", "AUD", "CHF" }, message = "产品币种类型参数错误")
	private String currency;// 币种
	
	
	@Enumerations(values = { "MANUALINPUT", "FIRSTRACKTIME" }, message = "产品成立时间类型参数错误")
	private String setupDateType;// 产品成立时间类型
	private String setupDate;// 成立时间
	
	
	@NotBlank(message = "收益结转周期不可为空")
	private String accrualCycleOid;// 收益结转周期
	@Range(min = 0)
	private int lockPeriod; // 锁定期
	
	@Range(min = 1)
	private int purchaseConfirmDate;// 申购确认日
	@Range(min = 1)
	private int interestsDate;// 起息日
	@Range(min = 1)
	private int redeemConfirmDate;// 赎回确认日
	
	
	@Digits(integer = 18, fraction = 2, message = "金额格式错误")
	private BigDecimal investMin;// 单笔投资最低份额
	@Digits(integer = 18, fraction = 2, message = "金额格式错误")
	private BigDecimal investAdditional;// 单笔投资追加份额
	@Digits(integer = 18, fraction = 2, message = "金额格式错误")
	private BigDecimal investMax;// 单笔投资最高份额
	
	
	private BigDecimal maxHold; //单人持有份额上限
	private BigDecimal netMaxRredeemDay;// 单日净赎回上限
	private BigDecimal singleDailyMaxRedeem;//单人单日赎回上限
	
	
	private BigDecimal minRredeem;// 单笔净赎回下限
	private BigDecimal additionalRredeem;  //单笔赎回递增份额
	private BigDecimal maxRredeem; // 单笔净赎回上限
	
	
	private BigDecimal netUnitShare;// 单位份额净值
	private String basicProductLabel;//基础标签
	private String[] expandProductLabels;//扩展标签
	
	
	private String dealStartTime;//交易开始时间
	private String dealEndTime;//交易结束时间
	private String investDateType; //有效投资日类型
	private String rredeemDateType; //有效赎回日类型
	
	
	@NotBlank
	@Enumerations(values = { "YES", "NO" }, message = "额外增信参数错误,只能是有或无")
	private String reveal;// 额外增信
	private String revealComment;// 增信备注
	private String instruction;// 产品说明
	private String investComment;// 投资标的
	private String riskLevel;// 风险等级
	private String investorLevel;// 投资者类型
	
	private String files;// 附加文件
	private String investFile;// 投资协议书
	private String serviceFile;// 信息服务协议
	
	private Integer singleDayRedeemCount; // 单人单日赎回次数
	
	//红包相关
	private Integer useRedPackages;//1:可以使用红包 2:不能使用红包,3:可以使用全部红包
	
	private Integer useraiseRateCoupons;//1:可以使用加息券 2:不能使用加息券,3:可以使用全部加息券
	
	private String[] redPackages;//红包ids
	
	private String[] raiseRateCoupons;//加息券ids
	@NotNull(message = "产品要素的ID不能为空")
	private String productElement;//产品要素的ID
	@NotNull(message = "产品说明ID不能为空")
	private String productIntro;//产品说明ID
	private String activityDetail;//活动产品详情介绍ID
	@NotNull(message = "是否为活动产品不能为空")
	private Integer isActivityProduct;//是否为活动产品
	private BigDecimal expectedArrorDisp;//折合年化收益率

}
