package com.guohuai.ams.product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.guohuai.component.web.parameter.validation.Enumerations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavePeriodicProductForm implements Serializable {

	private static final long serialVersionUID = 6251184821616489757L;

	private String oid;
	@NotEmpty(message = "产品编号不能为空")
	private String code; //产品编号
	
	@NotEmpty(message = "产品简称不能为空")
	private String name;//产品简称
	
	@NotEmpty(message = "产品全称不能为空")
	private String fullName;//产品全称
	
	@NotEmpty(message = "产品管理人不能为空")
	private String administrator;//产品管理人
	
	@Enumerations(values = {"PRODUCTTYPE_01"}, message = "产品类型错误")
	private String typeOid;//产品类型
	
	@NotEmpty(message = "所属资产池不能为空")
	private String assetPoolOid;//资产池
	
	@NotNull(message = "起始预期年化收益率不能为空")
	private BigDecimal expAror;//预期年化收益率
	private BigDecimal expArorSec;//预期年化收益率区间
	private BigDecimal rewardInterest; // 平台奖励收益
	
	@Enumerations(values = {"360", "365"}, message = "收益计算基础录入有误")
	private String incomeCalcBasis;//收益计算基础
	
	private BigDecimal operationRate;//平台运营费率
	
	@Enumerations(values = { "CNY", "USD", "EUR", "JPY", "GBP", "HKD", "SGD", "JMD", "AUD", "CHF" }, message = "产品币种类型参数错误")
	private String currency;//币种
	
	@Enumerations(values = { "MANUALINPUT", "FIRSTRACKTIME" }, message = "募集开始时间类型参数错误")
	private String raiseStartDateType;//募集开始时间类型
	
	private Date raiseStartDate;//募集开始时间
	
	@NotNull(message = "募集总份额不能为空")
	private BigDecimal raisedTotalNumber;//募集总份额
	
	@NotNull(message = "认购确认日不能为空")
	private Integer subscribeConfirmDays;// 认购确认日:认购订单提交后()个日内 
	
	@NotNull(message = "募集期不能为空")
	private Integer raisePeriod;//募集期:()个自然日
	
	private BigDecimal recPeriodExpAnYield;//募集期预期年化收益
	
	@NotNull(message = "募集期满后最晚成立日不能为空")
	private Integer foundDays;//募集期满后最晚成立日
	
	@NotNull(message = "起息日不能为空")
	private Integer interestsFirstDate;//起息日:募集满额后()个自然日
	
	@NotNull(message = "存续期不能为空")
	private Integer durationPeriod;//存续期:()个自然日
	
	@NotNull(message = "还本付息期不能为空")
	private Integer accrualDate;//还本付息日 存续期结束后第()个自然日
	
	@Enumerations(values = {Product.RAISE_FULL_FOUND_TYPE_MANUAL, Product.RAISE_FULL_FOUND_TYPE_AUTO}, message = "募集满额后是否自动触发成立输入有误")
	private String raiseFullFoundType;//募集满额后是否自动触发成立
	
	private Integer autoFoundDays;//募集满额后第()个自然日后自动成立
	
	
	private BigDecimal investMin;//单笔投资最低份额
	@NotNull(message = "单笔投资追加份额不能为空")
	private BigDecimal investAdditional;//单笔投资追加份额
	
	private BigDecimal investMax;//单笔投资最高份额
	
	private BigDecimal netUnitShare;//单位份额净值
	
	@NotEmpty(message = "基础标签不能为空")
	private String basicProductLabel;//基础标签
	
	private String[] expandProductLabels;//扩展标签
	
	private String dealStartTime;//交易开始时间
	private String dealEndTime;//交易结束时间
	
	@Enumerations(values = {Product.Product_dateType_T, Product.Product_dateType_D}, message = "投资日类型错误")
	private String investDateType;
	
	@Enumerations(values = { "YES", "NO" }, message = "额外增信参数错误,只能是有或无")
	private String reveal;//额外增信
	
	private String revealComment;//增信备注
	
	private String instruction;//产品说明
	
	private String investComment;//投资标的
	
	private String riskLevel;//风险等级
	
	private String investorLevel;//投资者类型
	
	private String files;//附加文件
	private String investFile;//投资协议书
	private String serviceFile;//信息服务协议
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
	private Integer ifP2P = 0; // 是否为p2p产品

	// 产品分类
	@NotNull(message = "产品分类不能为空")
	private Integer isP2PAssetPackage = 0;
	
}
