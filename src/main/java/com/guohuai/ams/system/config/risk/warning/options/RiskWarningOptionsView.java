package com.guohuai.ams.system.config.risk.warning.options;

import lombok.Data;

@Data
public class RiskWarningOptionsView {

	private String cateOid;
	private String cateTitle;
	private boolean showCate;

	private String indicateOid;
	private String indicateTitle;
	private String indicateDataType;
	private String indicateDataUnit;
	private boolean showIndicate;
	

	private String warningOid;
	private String waringTitle;
	private boolean showWarning;

	private String optionsOid;
	private String optionsTitle;
	private String optionsWlevel;
	private boolean showOptions;

}
