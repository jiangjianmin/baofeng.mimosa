package com.guohuai.ams.productPackage.coupon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.coupon.ProductCouponEnum;
import com.guohuai.basic.component.exception.GHException;
@Service
public class ProductPackageCouponService {
	
	@Autowired
	private ProductPackageCouponDao productPackageCouponDao;
	
	@Transactional
	public void saveEntity(String productOid, String[] redPackages, String[] raiseRateCoupons) {
		List<ProductPackageCoupon> list = new ArrayList<ProductPackageCoupon>();
		if(redPackages!=null)
			for(String red : redPackages){
				ProductPackageCoupon productCoupon = new ProductPackageCoupon();
				productCoupon.setCardOid(Integer.valueOf(red));
				productCoupon.setCreateTime(new Date());
				productCoupon.setProductOid(productOid);
				productCoupon.setType(ProductCouponEnum.redCoupon.getCode());
				list.add(productCoupon);
			}
		if(raiseRateCoupons!=null)
			for(String raise:raiseRateCoupons){
				ProductPackageCoupon productCoupon = new ProductPackageCoupon();
				productCoupon.setCardOid(Integer.valueOf(raise));
				productCoupon.setCreateTime(new Date());
				productCoupon.setProductOid(productOid);
				productCoupon.setType(ProductCouponEnum.raiseRateCoupon.getCode());
				list.add(productCoupon);
			}
		productPackageCouponDao.save(list);
		
	}
	@Transactional
	public void updateEntity(String productOid, String[] redPackages, String[] raiseRateCoupons) {
		int i = productPackageCouponDao.deleteByProductOid(productOid);
//		if(i<=0){
//			throw new GHException("删除产品包关联卡券失败");
//		}
		saveEntity(productOid,redPackages,raiseRateCoupons);
	}
	
	public List<Integer> getRedListByProductPackageOid(String oid) {
		return productPackageCouponDao.findRedByProductPackageOid(oid);
	}
	public List<Integer> getRaiseListByProductPackageOid(String oid) {
		return productPackageCouponDao.findRaiseByProductPackageOid(oid);
	}

}
