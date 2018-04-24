package com.guohuai.ams.label;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.product.Product;


public interface LabelDao extends JpaRepository<LabelEntity, String>, JpaSpecificationExecutor<LabelEntity> {

	/**
	 * 根据产品ID查询可用的基本标签
	 * @param productId
	 * @return
	 */
	@Query(value="SELECT count(*) FROM T_MONEY_PLATFORM_LABEL a  INNER JOIN T_MONEY_PLATFORM_LABEL_PRODUCT b ON a.oid=b.labelOid"
			+ " WHERE a.isOk='yes' AND b.productOid=?1  and a.labelCode='experienceFund' ", nativeQuery = true)
	public Integer findLabelByProductId(String productId);
	

	/**
	 * 根据产品ID查询可用标签Code
	 * @return
	 */
	@Query(value="SELECT labelCode,labelName "
			+ "FROM T_MONEY_PLATFORM_LABEL a "
			+ "INNER JOIN T_MONEY_PLATFORM_LABEL_PRODUCT b ON a.oid=b.labelOid "
			+ "WHERE a.isOk='yes' AND b.productOid=?1", nativeQuery = true)
	public List<Object[]> findLabelCodeByProductId(String productId);
	
	/**
	 * 根据标签oid查询可用标签code
	 * @return
	 */
	@Query(value="SELECT labelCode "
			+ "FROM T_MONEY_PLATFORM_LABEL "
			+ "WHERE oid=?1", nativeQuery = true)
	public String findLabelCodeByOid(String oid);




	/**
	 *
	 *根据产品id和lableCode查找lableOid
	 * @author yujianlong
	 * @date 2018/4/4 18:17
	 * @param [productId, lableCode]
	 * @return java.lang.String
	 */
	@Query(value=" SELECT "
			+" a.oid "
			+" FROM "
			+" T_MONEY_PLATFORM_LABEL a "
			+" INNER JOIN T_MONEY_PLATFORM_LABEL_PRODUCT b ON a.oid = b.labelOid "
			+" WHERE "
			+" a.isOk = 'yes' "
			+" AND b.productOid =?1 ", nativeQuery = true)
	public List<String> findLabelCodeByProdcuctOid( String productId);




	
	/**
	 * 根据标签oid查询标签实体
	 */
	public LabelEntity findByOid(String oid);
}
