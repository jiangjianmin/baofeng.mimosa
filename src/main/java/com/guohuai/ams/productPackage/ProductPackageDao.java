package com.guohuai.ams.productPackage;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProductPackageDao extends JpaRepository<ProductPackage, String>, JpaSpecificationExecutor<ProductPackage> {

	/**
	 * 根据产品编号获取产品实体
	 * 
	 * @param productCode
	 * @return {@link Product}
	 */
	public ProductPackage findByCode(String code);

	public List<ProductPackage> findByOidIn(List<String> oids);

	public List<ProductPackage> findByState(String state);

	@Query(value = "from ProductPackage where state ='Durationend' and repayLoanStatus = 'toRepay' and repayDate < ?1 and overdueStatus != 'yes' ")
	public List<ProductPackage> getOverdueProductPackage(Date curDate);
	
	/**
	 * 获取在售中的产品包
	 * @return
	 */
	@Query(value = "from ProductPackage WHERE auditState='REVIEWED' and state='REVIEWPASS' and isDeleted = 'NO' and raiseStartDate <= ?1 and raiseEndDate >= ?1 ")
	public List<ProductPackage> findOnSaleProductPackages(Date curDate);
	
	/**
	 * 获取募集未结束的产品包
	 * @return
	 */
	@Query(value = "from ProductPackage WHERE isDeleted = 'NO' and assetPoolOid = ?1 and raiseEndDate >= ?2 ")
	public List<ProductPackage> findProductPackages(String assetPoolOid,Date curDate);
	
	/**
	 * 更新toProductNum，记录产品包的下上架的产品数量
	 * @param oid
	 * @return
	 */
	@Query("update ProductPackage set toProductNum = toProductNum+1, maxSaleVolume = maxSaleVolume - ?2, updateTime = sysdate() where oid = ?1 ")
	@Modifying
	public int increaseToProductNum(String oid,BigDecimal singleProductVolume);
	
	@Query("from ProductPackage where guessOid = ?1 and isDeleted = 'NO'")
	public ProductPackage findByGuessOid(String guessOid);
	
}
