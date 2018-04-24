package com.guohuai.ams.product;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ProductRaisingIncomeDao extends JpaRepository<ProductRaisingIncomeEntity, String>, JpaSpecificationExecutor<ProductRaisingIncomeEntity> {

	@Query(value = " select sum(incomeAmount) incomeAmount from t_money_product_raising_income where productOid=?1 ", nativeQuery=true)
	public BigDecimal queryProductRaisingIncome(String productOid);
	
}
