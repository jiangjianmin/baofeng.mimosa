package com.guohuai.ams.product.order.salePosition;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ProductSaleScheduleDao
		extends JpaRepository<ProductSaleScheduling, String>, JpaSpecificationExecutor<ProductSaleScheduling> {
	
	@Query(value = "SELECT SUM(approvalAmount) AS approvalAmount"
	+ " FROM T_GAM_PRODUCT_SALE_SCHEDULING"
			+ " WHERE productOid = ?1 GROUP BY productOid", nativeQuery = true)
	public BigDecimal findApprovalAmountByProductOid(String productOid);

}
