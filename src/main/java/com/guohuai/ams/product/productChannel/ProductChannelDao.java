package com.guohuai.ams.product.productChannel;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.product.Product;

public interface ProductChannelDao extends JpaRepository<ProductChannel, String>, JpaSpecificationExecutor<ProductChannel> {

	public List<ProductChannel> findByProduct(Product product);

	@Query("from ProductChannel a where a.product.oid = ?1")
	public List<ProductChannel> findByProductOid(String productOid);

	@Query(value = "SELECT COUNT(*) FROM `T_GAM_PRODUCT_CHANNEL` a LEFT JOIN `T_MONEY_PLATFORM_CHANNEL` b ON a.channelOid = b.oid "
			+ "WHERE a.productOid = ?3 AND b.cid = ?1 AND b.ckey = ?2 AND a.status = 'VALID'", nativeQuery = true)
	public int countForPublish(String cid, String ckey, String productOid);
	
	
	@Query(value = "SELECT a.channelOid,a.productOid,b.cid,b.ckey,b.channelName "
			+ "FROM `T_GAM_PRODUCT_CHANNEL` a "
			+ "LEFT JOIN `T_MONEY_PLATFORM_CHANNEL` b ON a.channelOid = b.oid "
			+ "WHERE a.marketState='ONSHELF' and a.status='VALID' ", nativeQuery = true) 
	public List<Object[]> getChannelByBatch();
	
	@Query("from ProductChannel a where a.channel.oid = ?1")
	public List<ProductChannel> findByChannelOid(String oid);

}
