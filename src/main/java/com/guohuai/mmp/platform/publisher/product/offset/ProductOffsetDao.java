package com.guohuai.mmp.platform.publisher.product.offset;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.product.Product;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetEntity;

public interface ProductOffsetDao extends JpaRepository<ProductOffsetEntity, String>, JpaSpecificationExecutor<ProductOffsetEntity> {
	
	@Query(value = "from ProductOffsetEntity where product = ?1 and offsetCode = ?2")
	ProductOffsetEntity getLatestOffset(Product product, String offsetCode);
	
	
	@Query("update ProductOffsetEntity set investAmount = investAmount + ?2,"
			+ " netPosition = investAmount - redeemAmount where oid = ?1")
	@Modifying
	public int increaseInvest(String oid, BigDecimal investAmount);
	
	@Query("update ProductOffsetEntity set redeemAmount = redeemAmount + ?2,netPosition = investAmount - redeemAmount where oid = ?1")
	@Modifying
	public int increaseRedeem(String oid,BigDecimal redeemAmount);

	@Query(value = "update T_MONEY_PLATFORM_PUBLISHER_PRODUCT_OFFSET set closeStatus = 'closed' where publisherOid = ?1", nativeQuery = true)
	@Modifying
	int updateOffsetStatus2closed(String spvOid);
	
	/**
	 * 查询待清算产品
	 * @param product
	 * @return
	 */
	@Query(value = "from ProductOffsetEntity where product = ?1 and clearStatus = 'toClear' ")
	List<ProductOffsetEntity> findByProduct(Product product);

	
	@Query(value = "update T_MONEY_PLATFORM_PUBLISHER_PRODUCT_OFFSET set confirmStatus = ?2 "
			+ "where offsetOid = ?1 and confirmStatus = 'confirming' ", nativeQuery = true)
	@Modifying
	int updateConfirmStatus(String pOffsetOid, String confirmStatus);
	
	@Query(value = "update T_MONEY_PLATFORM_PUBLISHER_PRODUCT_OFFSET set confirmStatus = ?2 "
			+ "where offsetOid = ?1 and clearStatus = 'cleared' and confirmStatus in ('toConfirm', 'confirmFailed') ", nativeQuery = true)
	@Modifying
	int updateConfirmStatus4Lock(String pOffsetOid, String confirmStatus);
	
	List<ProductOffsetEntity> findByPublisherOffset(PublisherOffsetEntity findByOid);

	@Query(value = "update T_MONEY_PLATFORM_PUBLISHER_PRODUCT_OFFSET set clearStatus = ?2"
			+ " where offsetOid = ?1 and clearStatus = 'toClear'", nativeQuery = true)
	@Modifying
	int updateClearStatus(String offsetOid, String clearStatus);

	@Query(value = "update T_MONEY_PLATFORM_PUBLISHER_PRODUCT_OFFSET set closeStatus = ?2 "
			+ " where offsetOid = ?1 and clearStatus = 'cleared' and closeStatus != 'closed' ", nativeQuery = true)
	@Modifying
	int updateCloseStatus4Close(String offsetOid, String closeStatus);
	
	@Query(value = "update T_MONEY_PLATFORM_PUBLISHER_PRODUCT_OFFSET set closeStatus = ?2 "
			+ "where offsetOid = ?1 and closeStatus != 'closed' ", nativeQuery = true)
	@Modifying
	int updateCloseStatus4CloseBack(String offsetOid, String closeStatus);


	ProductOffsetEntity findByProductAndOffsetCode(Product product, String offsetCode);

	@Query(value = "select * from T_MONEY_PLATFORM_PUBLISHER_PRODUCT_OFFSET where productOid = ?1 and clearStatus = 'toClear' ", nativeQuery = true)
	List<ProductOffsetEntity> findByProductConstantly(String productOid);


	

	
	

}
