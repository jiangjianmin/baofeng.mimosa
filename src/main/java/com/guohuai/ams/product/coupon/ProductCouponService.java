package com.guohuai.ams.product.coupon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.productPackage.ProductPackage;
import com.guohuai.basic.component.exception.GHException;
@Service
public class ProductCouponService {
	
	@Autowired
	private ProductCouponDao productCouponDao;
	
	@Transactional
	public void saveEntity(String productOid, String[] redPackages, String[] raiseRateCoupons) {
		List<ProductCoupon> list = new ArrayList<ProductCoupon>();
		if(redPackages!=null)
			for(String red : redPackages){
				ProductCoupon productCoupon = new ProductCoupon();
				productCoupon.setCardOid(Integer.valueOf(red));
				productCoupon.setCreateTime(new Date());
				productCoupon.setProductOid(productOid);
				productCoupon.setType(ProductCouponEnum.redCoupon.getCode());
				list.add(productCoupon);
			}
		if(raiseRateCoupons!=null)
			for(String raise:raiseRateCoupons){
				ProductCoupon productCoupon = new ProductCoupon();
				productCoupon.setCardOid(Integer.valueOf(raise));
				productCoupon.setCreateTime(new Date());
				productCoupon.setProductOid(productOid);
				productCoupon.setType(ProductCouponEnum.raiseRateCoupon.getCode());
				list.add(productCoupon);
			}
		productCouponDao.save(list);
		
	}
	@Transactional
	public void updateEntity(String productOid, String[] redPackages, String[] raiseRateCoupons) {
		int i = productCouponDao.deleteByProductOid(productOid);
//		if(i<=0){
//			throw new GHException("删除产品关联卡券失败");
//		}
		saveEntity(productOid,redPackages,raiseRateCoupons);
	}
	
	
	@Transactional
	public void saveRatedProductCoupon(ProductPackage productPackage, Product p) {
		int i = productCouponDao.saveRatedProductCoupon(productPackage.getOid(),p.getOid());
		if(i<=0){
			throw new GHException("从产品包创建产品异常 productPackageOid:"+productPackage.getOid()+",productOid:"+p.getOid());
		}
	}
	
	public List<Integer> getRedListByProductOid(String oid) {
		
		return productCouponDao.findRedByProductOid(oid);
	}
	public List<Integer> getRaiseListByProductOid(String oid) {
		return productCouponDao.findRaiseByProductOid(oid);
	}
	
	

	public boolean canUserCardByProductOid(String productOid, Integer cardOid, Integer type) {
		return productCouponDao.findCountByProductAndCard(productOid, cardOid, type) > 0;
	}
}
