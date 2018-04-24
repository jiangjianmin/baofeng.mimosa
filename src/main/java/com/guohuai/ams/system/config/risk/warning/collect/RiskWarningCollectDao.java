package com.guohuai.ams.system.config.risk.warning.collect;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.guohuai.ams.system.config.risk.warning.RiskWarning;

public interface RiskWarningCollectDao
		extends JpaRepository<RiskWarningCollect, String>, JpaSpecificationExecutor<RiskWarningCollect> {

	public List<RiskWarningCollect> findByRelative(String relative);

	public RiskWarningCollect findByRelativeAndRiskWarning(String relative, RiskWarning riskWarning);
}
