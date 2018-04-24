package com.guohuai.ams.productPackage.coupon;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProductPackageCouponDao extends JpaRepository<ProductPackageCoupon, String>, JpaSpecificationExecutor<ProductPackageCoupon> {
	
	@Modifying
	@Query(value = "delete from t_gam_product_package_cardvolume where productOid=?1", nativeQuery = true)
	int deleteByProductOid(String productOid);
	@Query(value = "select cardOid from t_gam_product_package_cardvolume where productOid=?1 and type = 1", nativeQuery = true)
	List<Integer> findRedByProductPackageOid(String oid);
	@Query(value = "select cardOid from t_gam_product_package_cardvolume where productOid=?1 and type = 2", nativeQuery = true)
	List<Integer> findRaiseByProductPackageOid(String oid);
	
	

}
