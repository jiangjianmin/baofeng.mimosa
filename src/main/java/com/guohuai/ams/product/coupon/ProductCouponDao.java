package com.guohuai.ams.product.coupon;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProductCouponDao extends JpaRepository<ProductCoupon, String>, JpaSpecificationExecutor<ProductCoupon> {
	
	@Modifying
	@Query(value = "insert into t_gam_product_cardvolume "
			+ "SELECT REPLACE(UUID(),'-','') oid,?2,cardOid,type,createTime,useRedPackages,useraiseRateCoupons "
			+ "from t_gam_product_cardvolume where productOid=?1", nativeQuery = true)
	int saveRatedProductCoupon(String packageOid, String productOid);
	@Modifying
	@Query(value = "delete from t_gam_product_cardvolume where productOid=?1", nativeQuery = true)
	int deleteByProductOid(String productOid);
	
	@Query(value = "select cardOid from t_gam_product_cardvolume where productOid=?1 and type = 1", nativeQuery = true)
	List<Integer> findRedByProductOid(String oid);
	@Query(value = "select cardOid from t_gam_product_cardvolume where productOid=?1 and type = 2", nativeQuery = true)
	List<Integer> findRaiseByProductOid(String oid);

	@Query(value = "select count(1) from t_gam_product_cardvolume where productOid=?1 and cardOid=?2 and type=?3", nativeQuery = true)
	int findCountByProductAndCard(String productOid, Integer cardOid, Integer type);
}
