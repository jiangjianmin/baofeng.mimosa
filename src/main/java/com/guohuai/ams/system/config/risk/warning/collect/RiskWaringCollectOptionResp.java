package com.guohuai.ams.system.config.risk.warning.collect;

import java.util.List;
import java.util.Set;

import com.guohuai.ams.system.config.risk.cate.RiskCate;
import com.guohuai.ams.system.config.risk.indicate.RiskIndicate;
import com.guohuai.ams.system.config.risk.warning.RiskWarning;
import com.guohuai.ams.system.config.risk.warning.options.RiskWarningOptions;

import lombok.Data;

@Data
public class RiskWaringCollectOptionResp {

	private Set<RiskWarning> riskWarningList;

	private Set<RiskIndicate> indicateList;

	private Set<RiskCate> cateList;
	
	List<RiskWarningOptions> optinList;
}
