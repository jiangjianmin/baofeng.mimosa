package com.guohuai.ams.product;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ProductTypeDetailDao extends JpaRepository<ProductTypeDetail, String>, JpaSpecificationExecutor<ProductTypeDetail> {
	public List<ProductTypeDetail> findByType(String type);
	@Query(value="select * from T_GAM_PRODUCT_DETAIL order by createTime desc", nativeQuery = true)
	public List<ProductTypeDetail> findAllOrderByCreateTimeDesc();
	/**
	 * 根据产品ID查询存续期定期收益
	 * @param productOid
	 * @return
	 */
	@Query(value = "select sum(incomeAmount) from T_MONEY_PRODUCT_RAISING_INCOME where productOid = ?1", nativeQuery = true)
	public BigDecimal findSumIncomeAmount(String productOid);
}
