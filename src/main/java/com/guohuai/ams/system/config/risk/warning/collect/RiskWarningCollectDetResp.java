package com.guohuai.ams.system.config.risk.warning.collect;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class RiskWarningCollectDetResp {

	public RiskWarningCollectDetResp(String title, String riskLevel,String riskData,String riskUnit,String handleLevel) {
		this.title = title;
		this.riskLevel = riskLevel;
		this.riskData = riskData;
		this.riskUnit = riskUnit;
		this.handleLevel = handleLevel;
	}

	private String title; // 预警指标标题
	private String riskLevel; // 预警等级
	private String riskData; // 预警数据
	private String riskUnit; //数据单位
	private String handleLevel;
}
