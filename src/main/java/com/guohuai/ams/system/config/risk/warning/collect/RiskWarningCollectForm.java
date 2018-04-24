package com.guohuai.ams.system.config.risk.warning.collect;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class RiskWarningCollectForm {

	@NotNull(message = "风控项id不能为空")
	private String riskWaring;
	@NotNull(message = "标的id不能为空")
	private String targetOid;
	@NotNull(message = "配置项id不能为空")
	private String optionOid;
	
	private String riskWaringDataSelect;
	
	private String riskWaringDataInput;
	@NotNull(message = "风控等级不能为空")
	private String wLevel;
}
