package com.guohuai.ams.duration.fact.calibration;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MarketAdjustOrderDao extends JpaRepository<MarketAdjustOrderEntity, String>, JpaSpecificationExecutor<MarketAdjustOrderEntity> {

	public List<MarketAdjustOrderEntity> findByAdjust(MarketAdjustEntity adjust);

}
