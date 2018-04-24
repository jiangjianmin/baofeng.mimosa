package com.guohuai.ams.product.order.operating;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductDao;
import com.guohuai.ams.product.ProductService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.operate.api.AdminSdk;
import com.guohuai.operate.api.objs.admin.AdminObj;


@Service
@Transactional
public class ProductOperatingService {

	@Autowired
	private ProductOperatingDao productOperatingDao;
	@Autowired
	private ProductDao productDao;
	@Autowired
	private ProductService productService;
	@Autowired
	private AdminSdk adminSdk;
	
	/**
	 * 获取指定id的产品申购，赎回开关打开关闭申请对象
	 * @param oid ProductOperatingOrder对象id
	 * @return {@link ProductOperatingOrder}
	 */
	public ProductOperatingOrder getProductOperatingOrderByOid(String oid) {
		ProductOperatingOrder poo = this.productOperatingDao.findOne(oid);
		if (poo == null || ProductOperatingOrder.STATUS_DELETE.equals(poo.getStatus())) {
			throw AMPException.getException(90003);
		}
		return poo;
	}
	
	/**
	 * ProductOperatingOrder列表
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public PageResp<ProductOperatingOrderResp> list(Specification<ProductOperatingOrder> spec, Pageable pageable) {
		Page<ProductOperatingOrder> poos = this.productOperatingDao.findAll(spec, pageable);
		
		PageResp<ProductOperatingOrderResp> pagesRep = new PageResp<ProductOperatingOrderResp>();
		
		if (poos != null && poos.getContent() != null && poos.getTotalElements() > 0) {
			
			Map<String, AdminObj> adminObjMap = new HashMap<String, AdminObj>();
			AdminObj adminObj = null;
			
			List<ProductOperatingOrderResp> rows = new ArrayList<ProductOperatingOrderResp>();
			for (ProductOperatingOrder poo : poos) {
				ProductOperatingOrderResp poor = new ProductOperatingOrderResp(poo);
				
				if(!StringUtil.isEmpty(poo.getCreator())) {
					if (adminObjMap.get(poo.getCreator()) == null) {
						try {
							adminObj = adminSdk.getAdmin(poo.getCreator());
							adminObjMap.put(poo.getCreator(), adminObj);
						} catch (Exception e) {
						}

					}
					if (adminObjMap.get(poo.getCreator()) != null) {
						poor.setCreator(adminObjMap.get(poo.getCreator()).getName());
					}
				}
				
				if(!StringUtil.isEmpty(poo.getAuditor())) {
					if (adminObjMap.get(poo.getAuditor()) == null) {
						try {
							adminObj = adminSdk.getAdmin(poo.getAuditor());
							adminObjMap.put(poo.getAuditor(), adminObj);
						} catch (Exception e) {
						}
					}
					if (adminObjMap.get(poo.getAuditor()) != null) {
						poor.setAuditor(adminObjMap.get(poo.getAuditor()).getName());
					}
				}
				rows.add(poor);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(poos.getTotalElements());
		return pagesRep;
	}
	
	/*
	 * 申请打开申购
	 */
	public BaseResp openPurchaseApply(final String oid, String operator) throws ParseException {
		BaseResp response = new BaseResp();
		
		Product p = productService.getProductByOid(oid);
		if (Product.YES.equals(p.getIsOpenPurchase())) {
			throw AMPException.getException(90020);
		}
		
		if (Product.APPLY_STATUS_ApplyOn.equals(p.getPurchaseApplyStatus())) {
			throw AMPException.getException(90024);
		}
		
		Specification<ProductOperatingOrder> spec = new Specification<ProductOperatingOrder>() {
			@Override
			public Predicate toPredicate(Root<ProductOperatingOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("product").get("oid").as(String.class), oid),
						cb.equal(root.get("type").as(String.class), ProductOperatingOrder.TYPE_PURCHASE_ON),
						cb.equal(root.get("status").as(String.class), ProductOperatingOrder.STATUS_SUBMIT));
			}
		};
		
		List<ProductOperatingOrder> pos = productOperatingDao.findAll(spec);

		if (pos == null || pos.size() == 0) {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			ProductOperatingOrder po = new ProductOperatingOrder();
			po.setOid(StringUtil.uuid());
			po.setProduct(p);
			po.setType(ProductOperatingOrder.TYPE_PURCHASE_ON);
			po.setCreator(operator);
			po.setCreateTime(now);
			po.setStatus(ProductOperatingOrder.STATUS_SUBMIT);
			productOperatingDao.save(po);
			
			p.setPurchaseApplyStatus(Product.APPLY_STATUS_ApplyOn);
			p.setUpdateTime(now);
			productDao.saveAndFlush(p);
			
		} else {
			throw AMPException.getException(90024);
		}

		return response;
	}
	
	/*
	 * 申请关闭申购
	 */
	public BaseResp closePurchaseApply(final String oid, String operator) throws ParseException {
		BaseResp response = new BaseResp();
		
		Product p = productService.getProductByOid(oid);
		if (Product.NO.equals(p.getIsOpenPurchase())) {
			throw AMPException.getException(90021);
		}
		
		if (Product.APPLY_STATUS_ApplyOff.equals(p.getPurchaseApplyStatus())) {
			throw AMPException.getException(90025);
		}
		
		Specification<ProductOperatingOrder> spec = new Specification<ProductOperatingOrder>() {
			@Override
			public Predicate toPredicate(Root<ProductOperatingOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("product").get("oid").as(String.class), oid),
						cb.equal(root.get("type").as(String.class), ProductOperatingOrder.TYPE_PURCHASE_OFF),
						cb.equal(root.get("status").as(String.class), ProductOperatingOrder.STATUS_SUBMIT));
			}
		};
		
		List<ProductOperatingOrder> pos = productOperatingDao.findAll(spec);

		if (pos == null || pos.size() == 0) {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			ProductOperatingOrder po = new ProductOperatingOrder();
			po.setOid(StringUtil.uuid());
			po.setProduct(p);
			po.setType(ProductOperatingOrder.TYPE_PURCHASE_OFF);
			po.setCreator(operator);
			po.setCreateTime(now);
			po.setStatus(ProductOperatingOrder.STATUS_SUBMIT);
			productOperatingDao.save(po);
			
			p.setPurchaseApplyStatus(Product.APPLY_STATUS_ApplyOff);
			p.setUpdateTime(now);
			productDao.saveAndFlush(p);
			
		} else {
			throw AMPException.getException(90025);
		}

		return response;
	}
	
	/*
	 * 申请打开赎回
	 */
	public BaseResp openRedeemApply(final String oid, String operator) throws ParseException {
		BaseResp response = new BaseResp();
		
		Product p = productService.getProductByOid(oid);
		if (Product.YES.equals(p.getIsOpenRemeed())) {
			throw AMPException.getException(90018);
		}
		
		if (Product.APPLY_STATUS_ApplyOn.equals(p.getRedeemApplyStatus())) {
			throw AMPException.getException(90018);
		}
		
		Specification<ProductOperatingOrder> spec = new Specification<ProductOperatingOrder>() {
			@Override
			public Predicate toPredicate(Root<ProductOperatingOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("product").get("oid").as(String.class), oid),
						cb.equal(root.get("type").as(String.class), ProductOperatingOrder.TYPE_REDEEM_ON),
						cb.equal(root.get("status").as(String.class), ProductOperatingOrder.STATUS_SUBMIT));
			}
		};
		
		List<ProductOperatingOrder> pos = productOperatingDao.findAll(spec);

		if (pos == null || pos.size() == 0) {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			ProductOperatingOrder po = new ProductOperatingOrder();
			po.setOid(StringUtil.uuid());
			po.setProduct(p);
			po.setType(ProductOperatingOrder.TYPE_REDEEM_ON);
			po.setCreator(operator);
			po.setCreateTime(now);
			po.setStatus(ProductOperatingOrder.STATUS_SUBMIT);
			productOperatingDao.save(po);
			
			p.setRedeemApplyStatus(Product.APPLY_STATUS_ApplyOn);
			p.setUpdateTime(now);
			productDao.saveAndFlush(p);
			
		} else {
			throw AMPException.getException(90022);
		}

		return response;
	}
	
	/*
	 * 申请关闭赎回
	 */
	public BaseResp closeRedeemApply(final String oid, String operator) throws ParseException {
		BaseResp response = new BaseResp();
		
		Product p = productService.getProductByOid(oid);
		if (Product.NO.equals(p.getIsOpenRemeed())) {
			throw AMPException.getException(90019);
		}
		
		if (Product.APPLY_STATUS_ApplyOff.equals(p.getRedeemApplyStatus())) {
			throw AMPException.getException(90023);
		}
		
		Specification<ProductOperatingOrder> spec = new Specification<ProductOperatingOrder>() {
			@Override
			public Predicate toPredicate(Root<ProductOperatingOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("product").get("oid").as(String.class), oid),
						cb.equal(root.get("type").as(String.class), ProductOperatingOrder.TYPE_REDEEM_OFF),
						cb.equal(root.get("status").as(String.class), ProductOperatingOrder.STATUS_SUBMIT));
			}
		};
		
		List<ProductOperatingOrder> pos = productOperatingDao.findAll(spec);

		if (pos == null || pos.size() == 0) {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			ProductOperatingOrder po = new ProductOperatingOrder();
			po.setOid(StringUtil.uuid());
			po.setProduct(p);
			po.setType(ProductOperatingOrder.TYPE_REDEEM_OFF);
			po.setCreator(operator);
			po.setCreateTime(now);
			po.setStatus(ProductOperatingOrder.STATUS_SUBMIT);
			productOperatingDao.save(po);
			
			p.setRedeemApplyStatus(Product.APPLY_STATUS_ApplyOff);
			p.setUpdateTime(now);
			productDao.saveAndFlush(p);
			
		} else {
			throw AMPException.getException(90023);
		}

		return response;
	}
	
	/*
	 * 审核通过-(申请打开申购,申请关闭申购,申请打开赎回,申请关闭赎回)
	 */
	public BaseResp passPurchaseRemeedApply(String oid, String operator) throws ParseException,Exception {
		BaseResp response = new BaseResp();
		
		ProductOperatingOrder po = this.getProductOperatingOrderByOid(oid);
		if(po.getProduct()==null || !ProductOperatingOrder.STATUS_SUBMIT.equals(po.getStatus())) {
			throw AMPException.getException(90013);
		}
		
		Product p = productService.getProductByOid(po.getProduct().getOid());
		
		if(ProductOperatingOrder.TYPE_PURCHASE_ON.equals(po.getType())) {//开启申购 PURCHASE_ON
			p.setPurchaseApplyStatus(Product.APPLY_STATUS_None);
			p.setIsOpenPurchase(Product.YES);
		} else if(ProductOperatingOrder.TYPE_PURCHASE_OFF.equals(po.getType())) {//关闭申购 PURCHASE_OFF
			p.setPurchaseApplyStatus(Product.APPLY_STATUS_None);
			p.setIsOpenPurchase(Product.NO);
		} else if(ProductOperatingOrder.TYPE_REDEEM_ON.equals(po.getType())) {//开启赎回 REDEEM_ON
			p.setRedeemApplyStatus(Product.APPLY_STATUS_None);
			p.setIsOpenRemeed(Product.YES);
		} else if(ProductOperatingOrder.TYPE_REDEEM_OFF.equals(po.getType())) {//关闭赎回 REDEEM_OFF
			p.setRedeemApplyStatus(Product.APPLY_STATUS_None);
			p.setIsOpenRemeed(Product.NO);
		}
		
		Timestamp now = new Timestamp(System.currentTimeMillis());
		po.setAuditor(operator);
		po.setAuditTime(now);
		po.setStatus(ProductOperatingOrder.STATUS_PASS);
		productOperatingDao.save(po);
		
		p.setUpdateTime(now);
		productDao.save(p);
		
//		/**
//		 * 同步信息到redis
//		 * @author yuechao
//		 */
//		cacheProductService.syncProductSwitch(p);
		
		return response;
	}
	
	/*
	 * 审核驳回-(申请打开申购,申请关闭申购,申请打开赎回,申请关闭赎回)
	 */
	public BaseResp failPurchaseRemeedApply(String oid, String operator) throws ParseException {
		BaseResp response = new BaseResp();
		
		ProductOperatingOrder po = this.getProductOperatingOrderByOid(oid);
		if(po.getProduct()==null || !ProductOperatingOrder.STATUS_SUBMIT.equals(po.getStatus())) {
			throw AMPException.getException(90013);
		}
		
		Product p = productService.getProductByOid(po.getProduct().getOid());

		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		po.setAuditor(operator);
		po.setAuditTime(now);
		po.setStatus(ProductOperatingOrder.STATUS_FAIL);
		productOperatingDao.save(po);
		
		if(ProductOperatingOrder.TYPE_PURCHASE_ON.equals(po.getType())) {//开启申购 PURCHASE_ON
			p.setPurchaseApplyStatus(Product.APPLY_STATUS_None);
		} else if(ProductOperatingOrder.TYPE_PURCHASE_OFF.equals(po.getType())) {//关闭申购 PURCHASE_OFF
			p.setPurchaseApplyStatus(Product.APPLY_STATUS_None);
		} else if(ProductOperatingOrder.TYPE_REDEEM_ON.equals(po.getType())) {//开启赎回 REDEEM_ON
			p.setRedeemApplyStatus(Product.APPLY_STATUS_None);
		} else if(ProductOperatingOrder.TYPE_REDEEM_OFF.equals(po.getType())) {//关闭赎回 REDEEM_OFF
			p.setRedeemApplyStatus(Product.APPLY_STATUS_None);
		}
		
		p.setUpdateTime(now);
		productDao.saveAndFlush(p);
			
		return response;
	}
	
	/*
	 * 删除-(申请打开申购,申请关闭申购,申请打开赎回,申请关闭赎回)
	 */
	public BaseResp deletePurchaseRemeedApply(String oid, String operator) throws ParseException {
		BaseResp response = new BaseResp();
		
		ProductOperatingOrder po = this.getProductOperatingOrderByOid(oid);
		
		if(!ProductOperatingOrder.STATUS_FAIL.equals(po.getStatus())) {
			throw AMPException.getException(90027);
		}
		
		if(po.getProduct()==null) {
			throw AMPException.getException(90013);
		}
		
		Product p = productService.getProductByOid(po.getProduct().getOid());

		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		po.setAuditor(operator);
		po.setAuditTime(now);
		po.setStatus(ProductOperatingOrder.STATUS_DELETE);
		productOperatingDao.save(po);
		
		if(ProductOperatingOrder.TYPE_PURCHASE_ON.equals(po.getType())) {//开启申购 PURCHASE_ON
			p.setPurchaseApplyStatus(Product.APPLY_STATUS_None);
		} else if(ProductOperatingOrder.TYPE_PURCHASE_OFF.equals(po.getType())) {//关闭申购 PURCHASE_OFF
			p.setPurchaseApplyStatus(Product.APPLY_STATUS_None);
		} else if(ProductOperatingOrder.TYPE_REDEEM_ON.equals(po.getType())) {//开启赎回 REDEEM_ON
			p.setRedeemApplyStatus(Product.APPLY_STATUS_None);
		} else if(ProductOperatingOrder.TYPE_REDEEM_OFF.equals(po.getType())) {//关闭赎回 REDEEM_OFF
			p.setRedeemApplyStatus(Product.APPLY_STATUS_None);
		}
		
		p.setUpdateTime(now);
		productDao.saveAndFlush(p);
			
		return response;
	}
	
	/*
	 * 撤销-(申请打开申购,申请关闭申购,申请打开赎回,申请关闭赎回)
	 */
	public BaseResp rollbackPurchaseRemeedApply(String oid, String operator) throws ParseException {
		BaseResp response = new BaseResp();
		
		ProductOperatingOrder po = this.getProductOperatingOrderByOid(oid);
		
		if(!ProductOperatingOrder.STATUS_SUBMIT.equals(po.getStatus())) {
			throw AMPException.getException(90028);
		}
		
		if(po.getProduct()==null) {
			throw AMPException.getException(90013);
		}
		
		Product p = productService.getProductByOid(po.getProduct().getOid());

		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		po.setAuditor(operator);
		po.setAuditTime(now);
		po.setStatus(ProductOperatingOrder.STATUS_CANCEL);
		productOperatingDao.save(po);
		
		if(ProductOperatingOrder.TYPE_PURCHASE_ON.equals(po.getType())) {//开启申购 PURCHASE_ON
			p.setPurchaseApplyStatus(Product.APPLY_STATUS_None);
		} else if(ProductOperatingOrder.TYPE_PURCHASE_OFF.equals(po.getType())) {//关闭申购 PURCHASE_OFF
			p.setPurchaseApplyStatus(Product.APPLY_STATUS_None);
		} else if(ProductOperatingOrder.TYPE_REDEEM_ON.equals(po.getType())) {//开启赎回 REDEEM_ON
			p.setRedeemApplyStatus(Product.APPLY_STATUS_None);
		} else if(ProductOperatingOrder.TYPE_REDEEM_OFF.equals(po.getType())) {//关闭赎回 REDEEM_OFF
			p.setRedeemApplyStatus(Product.APPLY_STATUS_None);
		}
			
		return response;
	}
	
}
