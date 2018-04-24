package com.guohuai.ams.product;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;

import com.guohuai.component.web.parameter.validation.Enumerations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentTradingRuleSetForm implements Serializable {

	private static final long serialVersionUID = -1900167283203989313L;
	
	@NotBlank
	private String oid;
	
	private String purchaseConfirmDate;// 申购确认日:()个
	@Enumerations(values = { "NATRUE", "TRADE" }, message = "申购确认日类型参数错误,只能是自然日或交易日")
	private String purchaseConfirmDateType;// 申购确认日类型:自然日或交易日
	
	private String interestsDate;// 起息日:募集满额后()个自然日
	
	private String redeemConfirmDate;// 赎回确认日:()个
	@Enumerations(values = { "NATRUE", "TRADE" }, message = "申赎回确认日类型参数错误,只能是自然日或交易日")
	private String redeemConfirmDateType;// 赎回确认日类型自然日或交易日
	
	private String netUnitShare;// 单位份额净值
	private String netMaxRredeemDay;// 单日净赎回上限
	private String maxHold;//单人持有份额上限
	private String singleDailyMaxRedeem;//单人单日赎回上限
	private String investMin;// 单笔投资最低份额
	private String investAdditional;// 单笔投资追加份额
	private String investMax;// 单笔投资最高份额
	private String minRredeem;// 单笔赎回最低份额
	private String additionalRredeem;//单笔赎回递增份额
	private String maxRredeem; //单笔赎回最高份额
    private String investDateType;
    private String rredeemDateType;
    private String dealStartTime;//交易开始时间
	private String dealEndTime;//交易结束时间
	private Integer singleDayRedeemCount; // 单人单日赎回次数


}
