package com.guohuai.mmp.investor.referprofit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.product.Product;

public interface ProfitRuleDao extends JpaRepository<ProfitRuleEntity, String>,JpaSpecificationExecutor<ProfitRuleEntity> {

	@Query(value = "SELECT * FROM T_MONEY_PROFIT_RULE ORDER BY CREATETIME DESC LIMIT 1", nativeQuery = true)
	public ProfitRuleEntity getProfitRule();
}
