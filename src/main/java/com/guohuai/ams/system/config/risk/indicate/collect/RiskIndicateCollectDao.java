package com.guohuai.ams.system.config.risk.indicate.collect;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskIndicateCollectDao extends JpaRepository<RiskIndicateCollect, String> {

	public List<RiskIndicateCollect> findByRelative(String relative);

	public void deleteByRelative(String relative);

}
