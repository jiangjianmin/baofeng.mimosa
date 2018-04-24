package com.guohuai.ams.product.reward;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.product.Product;

public interface ProductIncomeRewardDao extends JpaRepository<ProductIncomeReward, String>, JpaSpecificationExecutor<ProductIncomeReward> {

	@Query(value = "from ProductIncomeReward pir right join fetch pir.product where pir.product.oid in ?1")
	public List<ProductIncomeReward> findByProductOid(List<String> poids);
	
	@Query(value = "from ProductIncomeReward pir where pir.product.oid = ?1 order by startDate")
	public List<ProductIncomeReward> findByProductOid(String oid);
	
	/**
	 * 闭区间
	 * @author yuechao
	 * @param product
	 * @param daysBetween
	 * @return ProductIncomeReward
	 */
	@Query(value = "from ProductIncomeReward where product = ?1 and startDate - 1 <= ?2  and (endDate - 1 >= ?2 or endDate is null)")
	ProductIncomeReward findByProductAndHoldDays(Product product, int daysBetween);
	
	@Query(value = "SELECT COUNT(*) FROM T_GAM_INCOME_REWARD WHERE productOid = ?1 LIMIT 1",nativeQuery=true)
	public int hasRewardIncome(String productOid);
	
	@Query(value = "SELECT distinct productOid FROM T_GAM_INCOME_REWARD",nativeQuery=true)
	public List<String> getRewardProductOid();
}
